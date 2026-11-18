package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.common.entity.*;
import group.xuxiake.common.entity.show.RecycleShowList;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.FileMediaMapper;
import group.xuxiake.common.mapper.FileShareMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.common.util.FileUtil;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RecyclePurgeHelper;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.service.RecycleService;
import group.xuxiake.web.service.RouteService;
import group.xuxiake.web.service.UserService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("/recycleService")
public class RecycleServiceImpl implements RecycleService {

	@Resource
	private FileRecycleMapper recycleMapper;
	@Resource
	private UserFileMapper userFileMapper;
	@Resource
	private UserMapper userMapper;
	@Resource
	private UserService userService;
	@Resource
	private RouteService routeService;
	@Resource
	private AppConfiguration appConfiguration;
	@Resource
	private FileOriginMapper fileOriginMapper;
	@Resource
	private FileMediaMapper fileMediaMapper;
	@Resource
	private FileShareMapper fileShareMapper;
	@Resource
	private FastDFSClientWrapper fastDFSClientWrapper;

	/**
	 * 查询回收站列表
	 * @param page
	 * @return
	 */
	@Override
	public Result toRecycleList(Page page) {
		Result result = new Result();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		PageHelper.startPage(page.getPageNum(), page.getPageSize());
		List<RecycleShowList> list = recycleMapper.findByUserId(user.getId(), page.getFileRealName());
		PageInfo<RecycleShowList> pageInfo = new PageInfo<>(list);
		result.setData(pageInfo);
		return result;
	}

	/**
	 * 删除回收站文件(彻底删除文件，不能找回)
	 * @param id
	 * @return
	 */
	@Transactional
	@Override
	public Result delete(Integer id) {

		Result result = new Result();
		User principal = (User) SecurityUtils.getSubject().getPrincipal();
		FileRecycle recycle = id == null ? null : recycleMapper.selectByPrimaryKeyForUpdate(id);
		if (principal == null || recycle == null || !principal.getId().equals(recycle.getDeleteUserId())) {
			result.setCode(NetdiskErrMsgConstant.FORBIDDEN);
			return result;
		}
		if (!Integer.valueOf(NetdiskConstant.RECYCLE_STATUS_FILE_DEL).equals(recycle.getRecycleStatus())) {
			result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
			result.setMsg("回收站记录已处理");
			return result;
		}
		List<String> storagePaths = RecyclePurgeHelper.purge(recycle.getRecycleId(), recycle.getFileId(),
				userFileMapper, fileShareMapper, fileMediaMapper, fileOriginMapper, recycleMapper);

		// 事务提交后删除定时任务和无引用的 FastDFS 文件。
		cancelRecycleJobAfterCommit(recycle, storagePaths);
		return result;
	}

	/**
	 * 恢复文件
	 * @param recycleId
	 * @param fileKey
	 * @return
	 */
	@Transactional
	@Override
	public Result reback(Integer recycleId, String fileKey) {

		Result result = new Result();
		UserFile userFile = userFileMapper.findFileBySaveNameForReback(fileKey);
		User userThreadContext = (User) SecurityUtils.getSubject().getPrincipal();
		FileRecycle recycleRecord = recycleId == null ? null : recycleMapper.selectByPrimaryKeyForUpdate(recycleId);
		if (userThreadContext == null || userFile == null || recycleRecord == null
				|| !userThreadContext.getId().equals(userFile.getUserId())
				|| !userThreadContext.getId().equals(recycleRecord.getDeleteUserId())
				|| !userFile.getId().equals(recycleRecord.getFileId())) {
			result.setCode(NetdiskErrMsgConstant.FORBIDDEN);
			return result;
		}
		if (!Integer.valueOf(NetdiskConstant.RECYCLE_STATUS_FILE_DEL).equals(recycleRecord.getRecycleStatus())
				|| !Integer.valueOf(NetdiskConstant.FILE_STATUS_OF_DEL).equals(userFile.getStatus())
				|| recycleRecord.getOverTime() == null || recycleRecord.getOverTime().before(new Date())) {
			result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
			result.setMsg("回收站记录已处理或已过期");
			return result;
		}
		User user = userMapper.selectByPrimaryKeyForUpdate(userThreadContext.getId());

		FileOrigin fileOrigin = fileOriginMapper.findByUserFileId(userFile.getId());

		// 检查剩余空间
		Long usedMemory = user.getUsedMemory();
		Long totalMemory = user.getTotalMemory();
		Long availableMemory = totalMemory - usedMemory;
		Long sumSize = null;
		if (userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", userFile.getId());
			map.put("sumsize", null);
			userFileMapper.getSumsizeRbk(map);
			if (map.get("sumsize") == null) {
				sumSize = 0L;
			} else {
				sumSize = ((Number) map.get("sumsize")).longValue();
			}

			// 检查目标父目录下是否存在同名文件夹
			UserFile fileSearch = userFileMapper.findFileByRealName(userFile);
			if (fileSearch != null) {
				result.setCode(NetdiskErrMsgConstant.FILE_RESTORE_TARGET_DIR_EXIST_SAME_NAME_DIR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_TARGET_DIR_EXIST_SAME_NAME_DIR));
				return result;
			}
		} else {
			sumSize = fileOrigin.getFileSize();
		}
		usedMemory = usedMemory + sumSize;
		user.setUsedMemory(usedMemory);
		if (sumSize > availableMemory) {
			result.setCode(NetdiskErrMsgConstant.FILE_RESTORE_AVAILABLEMEMORY_NOT_ENOUGH);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_AVAILABLEMEMORY_NOT_ENOUGH));
			return result;
		}

		//检查父目录是否被删除
		if (userFile.getParentId() != -1) {
			UserFile oldParentFile = userFileMapper.selectByPrimaryKey(userFile.getParentId());
			if (oldParentFile.getStatus() != NetdiskConstant.FILE_STATUS_OF_NORMAL) {
				result.setData(NetdiskErrMsgConstant.FILE_RESTORE_MAKE_RESOURCES_DIR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_MAKE_RESOURCES_DIR));

				//检查用户目录是否存在"我的资源"文件夹

				UserFile userFileChek = new UserFile();
				userFileChek.setUserId(user.getId());
				userFileChek.setParentId(-1);
				userFileChek.setFileName("我的资源");
				// "我的资源"文件夹
				UserFile fileResources = userFileMapper.findFileByRealName(userFileChek);
				if(fileResources == null){

					// 用户目录不存在"我的资源"文件夹
					fileResources = new UserFile();
					fileResources.setUserId(user.getId());
					fileResources.setParentId(-1);
					fileResources.setFileName("我的资源");
					fileResources.setKey(FileUtil.makeFileKey());
					fileResources.setCreateTime(new Date());
					fileResources.setUpdateTime(new Date());
					fileResources.setIsDir(NetdiskConstant.FILE_IS_DIR);
					fileResources.setStatus(NetdiskConstant.FILE_STATUS_OF_NORMAL);
					userFileMapper.insertSelective(fileResources);
				}
				userFile.setParentId(fileResources.getId());
			}
		}

		//更新文件状态
		userFile.setUpdateTime(new Date());
		userFile.setStatus(NetdiskConstant.DATA_NORMAL_STATUS);
		userFileMapper.updateByKeySelective(userFile);

		if(userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR){
			userFileMapper.rebackDir(userFile.getId());
		}

		//更新用户所用空间
		userMapper.updateByPrimaryKeySelective(user);

		FileRecycle recycle = new FileRecycle();
		recycle.setRecycleId(recycleId);
		recycle.setRecycleStatus(NetdiskConstant.RECYCLE_STATUS_FILE_HAVE_BEEN_RESTORE);
		//更新回收站
		recycleMapper.updateByPrimaryKeySelective(recycle);

		// userService.updatePrincipal();

		// 删除定时任务
		cancelRecycleJobAfterCommit(recycle, null);

		return result;
	}

	private void cancelRecycleJobAfterCommit(FileRecycle recycle, List<String> storagePaths) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					routeService.postMsgToRoute(recycle.getRecycleId().toString(),
							appConfiguration.getDelJobPath(), recycle, Result.class);
				} catch (RuntimeException e) {
					log.error("删除回收站定时任务失败: {}", recycle.getRecycleId(), e);
				}
				if (storagePaths != null) {
					for (String storagePath : storagePaths) {
						try {
							fastDFSClientWrapper.deleteFile(storagePath);
						} catch (RuntimeException e) {
							log.error("删除 FastDFS 文件失败: {}", storagePath, e);
						}
					}
				}
			}
		});
	}

}
