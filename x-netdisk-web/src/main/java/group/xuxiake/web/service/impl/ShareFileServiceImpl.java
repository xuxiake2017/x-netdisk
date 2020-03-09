package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import group.xuxiake.common.entity.*;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.common.entity.show.ShareFileShowInfo;
import group.xuxiake.common.entity.show.ShareFileShowList;
import group.xuxiake.common.mapper.FileUploadMapper;
import group.xuxiake.common.mapper.ShareFileMapper;
import group.xuxiake.common.mapper.UserNetdiskMapper;
import group.xuxiake.web.service.ShareFileService;
import group.xuxiake.web.service.UserNetdiskService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@Service("shareFileService")
public class ShareFileServiceImpl implements ShareFileService {

	@Resource
	private ShareFileMapper shareFileMapper;
	@Resource
	private FileUploadMapper fileUploadMapper;
	@Autowired
	private HttpServletResponse response;
	@Resource
	private UserNetdiskMapper userNetdiskMapper;
	@Resource
	private UserNetdiskService userNetdiskService;
	@Resource
	private FastDFSClientWrapper fastDFSClientWrapper;
	@Resource
	private AppConfiguration appConfiguration;

	/**
	 * 创建分享文件
	 * @param fileSaveName
	 * @return
	 */
	@Override
	public Result shareFile(String fileSaveName) {

		Result result = new Result();

		FileUpload fileUpload = fileUploadMapper.findFileBySaveName(fileSaveName);
		Integer shareUserId = Integer.valueOf(fileUpload.getUploadUserId());
		String shareId = RandomCodeUtil.getRandomShareId();
		String sharePwd = RandomCodeUtil.getRandomSharePwd();

		if (shareFileMapper.findByShareId(shareId) != null) {
			shareId = RandomCodeUtil.getRandomShareId();
		}
		Date shareTime = new Date();
		ShareFile shareFile = new ShareFile();
		shareFile.setShareId(shareId);
		shareFile.setSharePwd(sharePwd);
		shareFile.setShareUserId(shareUserId);
		shareFile.setShareTime(shareTime);
		shareFile.setShareStatus(NetdiskConstant.SHARE_STATUS_OF_NORMAL);
		shareFile.setFileId(fileUpload.getId());
		shareFile.setAccessTimes(0);
		shareFile.setDownloadTimes(0);
		shareFile.setSaveTimes(0);

		shareFileMapper.insertSelective(shareFile);

		Map<String, Object> map = new HashMap<>();
		map.put("shareFile", shareFile);
		map.put("serverHost", appConfiguration.getCustomConfiguration().getServerHost());
		result.setData(map);

		return result;
	}

	/**
	 * 获取分享文件
	 * @param shareId
	 * @return
	 */
	@Override
	public Result getShareFile(String shareId) {

		Result result = new Result();
		if (shareId ==  null || "".equals(shareId)) {
			return Result.paramIsNull(result);
		}
		ShareFileShowInfo shareFileInfo = shareFileMapper.getShareFileInfo(shareId);
		if (shareFileInfo == null) {
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			return result;
		}
		result.setData(shareFileInfo);

		// 增加访问次数
		ShareFile shareFile = shareFileMapper.findByShareId(shareId);
		this.addAccessTimes(shareFile.getId());
		return result;
	}

	@Override
	public ShareFile findByShareId(String shareId) {
		return shareFileMapper.findByShareId(shareId);
	}

	@Override
	public Result findAll(Page page) {

		Result result = new Result();
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
		PageHelper.startPage(page.getPageNum(), page.getPageSize());
		List<ShareFileShowList> list = shareFileMapper.findAllByUserId(userNetdisk.getId(), page.getFileRealName());
		PageInfo<ShareFileShowList> pageInfo = new PageInfo<>(list);
		Map<String, Object> map = new HashMap<>();
		map.put("pageInfo", pageInfo);
		map.put("serverHost", appConfiguration.getCustomConfiguration().getServerHost());
		result.setData(map);

		return result;
		
	}

	@Override
	public Integer addAccessTimes(Integer id) {
		ShareFile shareFile = shareFileMapper.selectByPrimaryKey(id);
		shareFile.setAccessTimes(shareFile.getAccessTimes() + 1);
		return shareFileMapper.updateByPrimaryKeySelective(shareFile);
	}

	@Override
	public Integer addDownloadTimes(Integer id) {
		ShareFile shareFile = shareFileMapper.selectByPrimaryKey(id);
		shareFile.setDownloadTimes(shareFile.getDownloadTimes() + 1);
		return shareFileMapper.updateByPrimaryKeySelective(shareFile);
	}

	@Override
	public Integer addSaveTimes(Integer id) {
		ShareFile shareFile = shareFileMapper.selectByPrimaryKey(id);
		shareFile.setSaveTimes(shareFile.getSaveTimes() + 1);
		return shareFileMapper.updateByPrimaryKeySelective(shareFile);
	}

	@Override
	public Result deleteShare(Integer id) {

		Result result = new Result();
		if (id == null) {
			return Result.paramIsNull(result);
		}
		ShareFile shareFile = new ShareFile();
		shareFile.setId(id);
		shareFile.setShareStatus(NetdiskConstant.SHARE_STATUS_OF_DEL);
		shareFileMapper.updateByPrimaryKeySelective(shareFile);

		return result;
	}

	/**
	 * 检查提取密码
	 * @param shareId 分享文件shareId
	 * @param sharePwd 分享文件密码
	 * @return
	 */
	@Override
	public Result checkPwd(String shareId, String sharePwd) {

		Result result = new Result();
		if (shareId == null || "".equals(shareId) || sharePwd == null || "".equals(sharePwd)) {
			return Result.paramIsNull(result);
		}

		ShareFile shareFile = shareFileMapper.findByShareId(shareId);
		if (shareFile == null) {
			// 资源不存在
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			return result;
		}

		if (!shareFile.getSharePwd().equals(sharePwd)) {
			// 密码错误
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG));
			return result;
		}

		return result;
	}

	/**
	 * 分享文件下载
	 * @param shareId
	 * @param sharePwd
	 */
	@Override
	public void download(String shareId, String sharePwd, Integer fileId) {

		Result result = new Result();
		if (shareId == null || "".equals(shareId) || sharePwd == null || "".equals(sharePwd)) {
			result = Result.paramIsNull(result);
			this.printlnWithResponse(response, result);
			return;
		}

		ShareFile shareFile = shareFileMapper.findByShareId(shareId);
		if (shareFile == null) {
			// 资源不存在
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			this.printlnWithResponse(response, result);
			return;
		}

		if (!shareFile.getSharePwd().equals(sharePwd)) {
			// 密码错误
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG));
			this.printlnWithResponse(response, result);
			return;
		}
		FileUpload fileUpload = null;
		if (fileId == null) {
			fileUpload = fileUploadMapper.selectByPrimaryKey(shareFile.getFileId());
		} else {
			// 检查fileId是否合法
			List<Integer> childIds = fileUploadMapper.findChildIds(shareFile.getFileId());
			if (!childIds.contains(fileId)) {
				result.setCode(NetdiskErrMsgConstant.SHARE_FILE_DOWNLOAD_WRONG);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_DOWNLOAD_WRONG));
				this.printlnWithResponse(response, result);
				return;
			}
			fileUpload = fileUploadMapper.selectByPrimaryKey(fileId);
		}
		//增加下载次数
		this.addDownloadTimes(shareFile.getId());
		String filePath = fileUpload.getFilePath();
		OutputStream out = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(fastDFSClientWrapper.getInputStream(filePath));
			// 设置响应头，控制浏览器下载该文件
			response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileUpload.getFileRealName(), "UTF-8"));
			// 设置文件大小
			response.addHeader("Content-Length", fileUpload.getFileSize().toString());
			out = response.getOutputStream();
			byte buffer[] = new byte[1024];
			int len = 0;
			while ((len = bis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
			log.error("文件下载失败", e);
		} finally {
			try {
				if (bis!=null) {
					// 关闭文件输入流
					bis.close();
				}
				if (out!=null) {
					// 关闭输出流
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存分享文件到网盘
	 * @param shareId
	 * @param sharePwd
	 * @return
	 */
	@Override
	public Result saveToCloud(String shareId, String sharePwd, Integer fileId) {

		Result result = new Result();
		Subject subject = SecurityUtils.getSubject();
		if (!subject.isAuthenticated()) {
			// 未登录
			result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_IS_NOT_AUTHENTICATED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_IS_NOT_AUTHENTICATED));
			return result;
		}

		if (shareId == null || "".equals(shareId) || sharePwd == null || "".equals(sharePwd)) {
			return Result.paramIsNull(result);
		}

		ShareFile shareFile = shareFileMapper.findByShareId(shareId);

		if (shareFile == null) {
			// 资源不存在
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			return result;
		}

		if (!shareFile.getSharePwd().equals(sharePwd)) {
			// 密码错误
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG));
			return result;
		}

		// 要保存的文件或者是要保存的最外层文件夹
		FileUpload fileForSave = null;
		if (fileId != null) {
			fileForSave = fileUploadMapper.selectByPrimaryKey(fileId);
		} else {
			fileForSave = fileUploadMapper.selectByPrimaryKey(shareFile.getFileId());
		}

		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();

		if (fileForSave.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			// 检查用户目录是否存在"我的资源"文件夹
			FileUpload fileUploadChek = new FileUpload();
			fileUploadChek.setUploadUserId(userNetdisk.getId() + "");
			fileUploadChek.setParentId(-1);
			fileUploadChek.setFileRealName("我的资源");
			List<FileUpload> list = fileUploadMapper.findFileByRealName(fileUploadChek);
			if(list != null && list.size() > 0){
				FileUpload fileUpload = list.get(0);
				// 检查目标父目录下是否存在同名文件夹
				List<FileUpload> list2 = fileUploadMapper.findFileByRealName(fileUpload);
				if (list2 != null && list2.size() > 0) {
					// 目标目录存在同名文件夹，保存失败
					result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_TARGET_DIR_EXIST_SAME_NAME_DIR);
					result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_TARGET_DIR_EXIST_SAME_NAME_DIR));
					return result;
				}
			}
		}


		// 检查fileId是否合法
		if (fileId != null) {
			List<Integer> childIds = fileUploadMapper.findChildIds(shareFile.getFileId());
			if (!childIds.contains(fileId)) {
				result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_FAILED);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_FAILED));
				return result;
			}
		}

		// 查询剩余空间
		Map<String, Object> map = new HashMap<>();
		map.put("id", fileForSave.getId());
		map.put("sumsize", null);
		fileUploadMapper.getSumsizeDel(map);
		Long sumSize = null;
		if(map.get("sumsize") != null) {
			sumSize = Long.valueOf(map.get("sumsize") + "");
		}

		long totalMemory = Long.valueOf(userNetdisk.getTotalMemory());
		long usedMemory = Long.valueOf(userNetdisk.getUsedMemory());
		long availableMemory = totalMemory - usedMemory;
		if (availableMemory < sumSize) {

			//剩余空间不足
			result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH));
			return result;
		}
		usedMemory = usedMemory + sumSize;
		userNetdisk.setUsedMemory(usedMemory + "");
		userNetdiskMapper.updateByPrimaryKeySelective(userNetdisk);

		// 递归保存文件夹内的文件到网盘
		this.saveToCloudIteration(fileForSave.getId(), null, userNetdisk.getId(), shareFile);

		result.setMsg("文件已成功保存到\"我的资源\"文件夹！");

		// 更新用户信息
		userNetdiskService.updatePrincipal();
		// 增加保存次数
		this.addSaveTimes(shareFile.getId());
		return result;
	}

	/**
	 * 查询文件夹子目录
	 * @param shareId
	 * @param sharePwd
	 * @param parentId
	 * @return
	 */
	@Override
	public Result getSubList(String shareId, String sharePwd, Integer parentId) {

		Result result = new Result();
		if (shareId == null || "".equals(shareId) || sharePwd == null || "".equals(sharePwd)) {
			return Result.paramIsNull(result);
		}

		ShareFile shareFile = shareFileMapper.findByShareId(shareId);

		if (shareFile == null) {
			// 资源不存在
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			return result;
		}

		if (!shareFile.getSharePwd().equals(sharePwd)) {
			// 密码错误
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG));
			return result;
		}

		// 检查parentId是否合法
		if (parentId != null) {
			List<Integer> childIds = fileUploadMapper.findChildIds(shareFile.getFileId());
			if (!childIds.contains(parentId)) {
				result.setCode(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_LIST_WRONG);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_LIST_WRONG));
				return result;
			}
		}

		List<FileUpload> files = new ArrayList<>();
		if (parentId == null) {
			FileUpload fileUpload = fileUploadMapper.selectByPrimaryKey(shareFile.getFileId());
			files.add(fileUpload);
		} else {
			FileUpload fileUpload = new FileUpload();
			fileUpload.setUploadUserId(shareFile.getShareUserId() + "");
			fileUpload.setParentId(parentId);
			files = fileUploadMapper.findAllFile(fileUpload, null);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("nginxServer", appConfiguration.getFdfsNginxServer());
		map.put("files", files);
		result.setData(map);

		return result;
	}

	public void printlnWithResponse(HttpServletResponse response, Result result) {

		response.setContentType("application/json;charset=UTF-8");
		try(PrintWriter writer = response.getWriter()) {
			Gson gson = new Gson();
			writer.println(gson.toJson(result));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 递归保存文件夹内的文件到网盘
	 * @param oldParentId 旧文件父id
	 * @param newParentId 新文件父id
	 * @param userId 保存文件用户id
	 * @param shareFile 分享文件
	 */
	public void saveToCloudIteration(Integer oldParentId, Integer newParentId, Integer userId, ShareFile shareFile) {

		FileUpload fileUpload = fileUploadMapper.selectByPrimaryKey(oldParentId);
		fileUpload.setId(null);
		fileUpload.setFileSaveName(FileUtil.makeDirSaveName());
		fileUpload.setUploadTime(new Date());
		fileUpload.setUploadUserId(userId + "");

		if (newParentId == null) {
			// 检查用户目录是否存在"我的资源"文件夹
			FileUpload fileUploadChek = new FileUpload();
			fileUploadChek.setUploadUserId(userId + "");
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
		} else {
			fileUpload.setParentId(newParentId);
		}

		fileUploadMapper.insertSelective(fileUpload);

		if (fileUpload.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			// 为文件夹，继续递归
			FileUpload fileUploadSearch = new FileUpload();
			fileUploadSearch.setParentId(oldParentId);
			fileUploadSearch.setUploadUserId(shareFile.getShareUserId() + "");
			List<FileUpload> files = fileUploadMapper.findAllFile(fileUploadSearch, null);
			if (files != null && files.size() > 0) {
				for (FileUpload f : files) {
					this.saveToCloudIteration(f.getId(), fileUpload.getId(), userId, shareFile);
				}
			}
		}


	}
}
