package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.common.entity.*;
import group.xuxiake.common.entity.show.RecycleShowList;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.service.RecycleService;
import group.xuxiake.web.service.RouteService;
import group.xuxiake.web.service.UserService;
import group.xuxiake.web.util.*;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

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
	private UserMapper userNetdiskMapper;
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
		if (fileOrigin.getFileSize() > availableMemory) {
			result.setCode(NetdiskErrMsgConstant.FILE_RESTORE_AVAILABLEMEMORY_NOT_ENOUGH);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_AVAILABLEMEMORY_NOT_ENOUGH));
			return result;
		}
		Integer tag1 = null;

		//检查父目录是否被删除
		if (userFile.getParentId() != -1) {
			if (userFileMapper.selectByPrimaryKey(userFile.getParentId()).getStatus() != NetdiskConstant.FILE_STATUS_OF_NORMAL) {
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
				} else {
					userFile.setParentId(fileResources.getId());
				}
			}
		}

		// 检查目标父目录下是否存在同名文件夹
		UserFile fileSearch = userFileMapper.findFileByRealName(userFile);
		if (fileSearch != null) {
			result.setCode(NetdiskErrMsgConstant.FILE_RESTORE_TARGET_DIR_EXIST_SAME_NAME_DIR);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_TARGET_DIR_EXIST_SAME_NAME_DIR));
			return result;
		}

		if(userFile.getIsDir() == NetdiskConstant.FILE_IS_NOT_DIR){
			//如果是文件
			long fileSize = fileOrigin.getFileSize();
			usedMemory = usedMemory + fileSize;
			user.setUsedMemory(usedMemory);
			//更新文件状态
			userFile.setCreateTime(new Date());
			userFile.setUpdateTime(new Date());
			userFile.setStatus(NetdiskConstant.DATA_NORMAL_STATUS);
			userFileMapper.updateByKeySelective(userFile);

		}else if (userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			//如果是文件夹，还要恢复文件夹里面的子文件及子文件夹
			Map<String, Object> map = new HashMap<>();
			map.put("id", userFile.getId());
			map.put("sumsize", null);
			userFileMapper.getSumsizeRbk(map);
			if (map.get("sumsize") != null) {
				Long sumSize = Long.valueOf(map.get("sumsize")+"");
				usedMemory = usedMemory + sumSize;
				user.setUsedMemory(usedMemory);
			}
			userFile.setStatus(NetdiskConstant.FILE_STATUS_OF_DEL_PASSIVE);
			userFileMapper.updateByKeySelective(userFile);
			userFileMapper.rebackDir(userFile.getId());
		}
		//更新用户所用空间
		Integer tag2 = userNetdiskMapper.updateByPrimaryKeySelective(user);

		FileRecycle recycle = new FileRecycle();
		recycle.setRecycleId(recycleId);
		recycle.setRecycleStatus(NetdiskConstant.RECYCLE_STATUS_FILE_HAVE_BEEN_RESTORE);
		//更新回收站
		Integer tag3 = recycleMapper.updateByPrimaryKeySelective(recycle);

		userService.updatePrincipal();

		// 删除定时任务
		routeService.postMsgToRoute(recycle.getRecycleId().toString(), appConfiguration.getDelJobPath(), recycle, Result.class);

		return result;
	}

}
