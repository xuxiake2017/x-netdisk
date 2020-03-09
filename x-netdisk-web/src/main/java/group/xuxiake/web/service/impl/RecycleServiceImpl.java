package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.common.entity.*;
import group.xuxiake.common.entity.show.RecycleShowList;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.web.handler.RecycleHandler;
import group.xuxiake.common.mapper.FileUploadMapper;
import group.xuxiake.common.mapper.RecycleMapper;
import group.xuxiake.common.mapper.UserNetdiskMapper;
import group.xuxiake.web.service.RecycleService;
import group.xuxiake.web.service.UserNetdiskService;
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
	private RecycleMapper recycleMapper;
	@Resource
	private FileUploadMapper fileUploadMapper;
	@Resource
	private UserNetdiskMapper userNetdiskMapper;
	@Resource
	private UserNetdiskService userNetdiskService;

	/**
	 * 查询回收站列表
	 * @param page
	 * @return
	 */
	@Override
	public Result toRecycleList(Page page) {
		Result result = new Result();
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
		PageHelper.startPage(page.getPageNum(), page.getPageSize());
		List<RecycleShowList> list = recycleMapper.findByUserId(userNetdisk.getId(), page.getFileRealName());
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
		Recycle recycle = new Recycle();
		recycle.setRecycleId(id);
		recycle.setRecycleStatus(NetdiskConstant.RECYCLE_STATUS_FILE_HAVE_BEEN_DEL_FOREVER + "");
		recycleMapper.updateByPrimaryKeySelective(recycle);

		// 从内存库移除
		RecycleHandler.remove(id);
		return result;
	}

	/**
	 * 恢复文件
	 * @param recycleId
	 * @param fileSaveName
	 * @return
	 */
	@Override
	public Result reback(Integer recycleId, String fileSaveName) {

		Result result = new Result();
		FileUpload fileUpload = fileUploadMapper.findFileBySaveNameForReback(fileSaveName);
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();

		// 检查剩余空间
		Long usedMemory = Long.valueOf(userNetdisk.getUsedMemory());
		Long totalMemory = Long.valueOf(userNetdisk.getTotalMemory());
		Long availableMemory = totalMemory - usedMemory;
		if (Long.valueOf(fileUpload.getFileSize()) > availableMemory) {
			result.setCode(NetdiskErrMsgConstant.FILE_RESTORE_AVAILABLEMEMORY_NOT_ENOUGH);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_AVAILABLEMEMORY_NOT_ENOUGH));
			return result;
		}
		Integer tag1 = null;

		//检查父目录是否被删除
		if (fileUpload.getParentId() != -1) {
			if (new Integer(fileUploadMapper.selectByPrimaryKey(fileUpload.getParentId()).getFileStatus()) != NetdiskConstant.FILE_STATUS_OF_NORMAL) {
				result.setData(NetdiskErrMsgConstant.FILE_RESTORE_MAKE_RESOURCES_DIR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_MAKE_RESOURCES_DIR));

				//检查用户目录是否存在"我的资源"文件夹

				FileUpload fileUploadChek = new FileUpload();
				fileUploadChek.setUploadUserId(userNetdisk.getId() + "");
				fileUploadChek.setParentId(-1);
				fileUploadChek.setFileRealName("我的资源");
				List<FileUpload> list = fileUploadMapper.findFileByRealName(fileUploadChek);
				if(list.size() == 0){

					fileUploadChek.setFileSaveName(FileUtil.makeDirSaveName());
					fileUploadChek.setUploadTime(new Date());
					fileUploadChek.setFileType(NetdiskConstant.FILE_TYPE_OF_DIR);
					fileUploadChek.setIsDir(NetdiskConstant.FILE_IS_DIR);
					fileUploadMapper.insertSelective(fileUploadChek);
					fileUpload.setParentId(fileUploadChek.getId());
				}else {
					fileUpload.setParentId(list.get(0).getId());
				}
			}
		}

		// 检查目标父目录下是否存在同名文件夹
		List<FileUpload> list = fileUploadMapper.findFileByRealName(fileUpload);
		if (list != null && list.size() > 0) {
			result.setCode(NetdiskErrMsgConstant.FILE_RESTORE_TARGET_DIR_EXIST_SAME_NAME_DIR);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_TARGET_DIR_EXIST_SAME_NAME_DIR));
			return result;
		}

		if(fileUpload.getIsDir() == NetdiskConstant.FILE_IS_NOT_DIR){
			//如果是文件
			long fileSize = Long.valueOf(fileUpload.getFileSize());
			usedMemory = usedMemory + fileSize;
			userNetdisk.setUsedMemory(usedMemory + "");
			//更新文件状态
			fileUpload.setUploadTime(new Date());
			fileUpload.setFileStatus(NetdiskConstant.DATA_NORMAL_STATUS + "");
			tag1 = fileUploadMapper.updateFileSelective(fileUpload);

		}else if (fileUpload.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			//如果是文件夹，还要恢复文件夹里面的子文件及子文件夹
			Map<String, Object> map = new HashMap<>();
			map.put("id", fileUpload.getId());
			map.put("sumsize", null);
			fileUploadMapper.getSumsizeRbk(map);
			if (map.get("sumsize") != null) {
				Long sumSize = Long.valueOf(map.get("sumsize")+"");
				usedMemory = usedMemory + sumSize;
				userNetdisk.setUsedMemory(usedMemory+"");
			}
			fileUpload.setFileStatus(NetdiskConstant.FILE_STATUS_OF_DEL_PASSIVE + "");
			tag1 = fileUploadMapper.updateFileSelective(fileUpload);
			fileUploadMapper.rebackDir(fileUpload.getId());
		}
		//更新用户所用空间
		Integer tag2 = userNetdiskMapper.updateByPrimaryKeySelective(userNetdisk);

		Recycle recycle = new Recycle();
		recycle.setRecycleId(recycleId);
		recycle.setRecycleStatus(NetdiskConstant.RECYCLE_STATUS_FILE_HAVE_BEEN_RESTORE + "");
		//更新回收站
		Integer tag3 = recycleMapper.updateByPrimaryKeySelective(recycle);

		userNetdiskService.updatePrincipal();

		// 从内存库移除
		RecycleHandler.remove(recycleId);

		if (tag1>0 && tag2>0 && tag3>0) {
			return result;
		}else {
			result.setCode(NetdiskErrMsgConstant.FILE_RESTORE_FAILED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.FILE_RESTORE_FAILED));
			return result;
		}
	}

}
