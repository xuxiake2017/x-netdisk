package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.common.entity.*;
import group.xuxiake.common.entity.show.RecycleShowList;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.common.util.FileUtil;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.service.RecycleService;
import group.xuxiake.web.service.RouteService;
import group.xuxiake.web.service.UserService;
import group.xuxiake.web.util.*;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@Override
	public Result delete(Integer id) {

		Result result = new Result();
		FileRecycle recycle = new FileRecycle();
		recycle.setRecycleId(id);
		recycle.setRecycleStatus(NetdiskConstant.RECYCLE_STATUS_FILE_HAVE_BEEN_DEL_FOREVER);
		recycleMapper.updateByPrimaryKeySelective(recycle);

		// 删除定时任务
		routeService.postMsgToRoute(recycle.getRecycleId().toString(), appConfiguration.getDelJobPath(), recycle, Result.class);
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
		User user = (User) SecurityUtils.getSubject().getPrincipal();
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
				sumSize = new Long((Integer) map.get("sumsize"));
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
					fileResources.setFilePath("/");
					userFileMapper.insertSelective(fileResources);
				}
				userFile.setParentId(fileResources.getId());

				// 父目录更改，更新filePath
				String targetPatentFilePath = userFileMapper.findPathname(fileResources.getId());
				String nowParentFilePath = userFileMapper.findPathname(oldParentFile.getId());
				List<Integer> childIds = userFileMapper.findChildIds(userFile.getId());
				if (childIds.size() > 0) {
					List<UserFile> userFiles = userFileMapper.finFiledByIds(childIds);
					for (UserFile file : userFiles) {
						String filePath = file.getFilePath().replaceFirst("^" + nowParentFilePath, targetPatentFilePath);
						file.setFilePath(filePath);
					}
					if (userFiles.size() > 0) {
						userFileMapper.updateBatch(userFiles);
					}
				}
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

		userService.updatePrincipal();

		// 删除定时任务
		routeService.postMsgToRoute(recycle.getRecycleId().toString(), appConfiguration.getDelJobPath(), recycle, Result.class);

		return result;
	}

}
