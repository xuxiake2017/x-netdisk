package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import group.xuxiake.common.entity.*;
import group.xuxiake.common.entity.show.FileShowInfo;
import group.xuxiake.common.entity.show.FileShowMedia;
import group.xuxiake.common.mapper.*;
import group.xuxiake.common.util.FileUtil;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.common.entity.show.ShareFileShowInfo;
import group.xuxiake.common.entity.show.ShareFileShowList;
import group.xuxiake.web.service.ShareFileService;
import group.xuxiake.web.service.UserService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private FileShareMapper fileShareMapper;
	@Resource
	private UserFileMapper userFileMapper;
	@Autowired
	private HttpServletResponse response;
	@Resource
	private UserMapper userMapper;
	@Resource
	private UserService userService;
	@Resource
	private FastDFSClientWrapper fastDFSClientWrapper;
	@Resource
	private AppConfiguration appConfiguration;
	@Resource
	private FileOriginMapper fileOriginMapper;
	@Resource
	private FileMediaMapper fileMediaMapper;

	/**
	 * 创建分享文件
	 * @param fileKey
	 * @return
	 */
	@Transactional
	@Override
	public Result shareFile(String fileKey) {

		Result result = new Result();

		UserFile fileUpload = userFileMapper.findFileByKey(fileKey);
		Integer shareUserId = fileUpload.getUserId();
		String shareId = RandomCodeUtil.getRandomShareId();
		String sharePwd = RandomCodeUtil.getRandomSharePwd();

		if (fileShareMapper.findByShareId(shareId) != null) {
			shareId = RandomCodeUtil.getRandomShareId();
		}
		Date shareTime = new Date();
		FileShare fileShare = new FileShare();
		fileShare.setShareId(shareId);
		fileShare.setSharePwd(sharePwd);
		fileShare.setShareUserId(shareUserId);
		fileShare.setShareTime(shareTime);
		fileShare.setShareStatus(NetdiskConstant.SHARE_STATUS_OF_NORMAL);
		fileShare.setFileId(fileUpload.getId());
		fileShare.setAccessTimes(0);
		fileShare.setDownloadTimes(0);
		fileShare.setSaveTimes(0);

		fileShareMapper.insertSelective(fileShare);

		Map<String, Object> map = new HashMap<>();
		map.put("shareFile", fileShare);
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
		ShareFileShowInfo shareFileInfo = fileShareMapper.getShareFileInfo(shareId);
		if (shareFileInfo == null) {
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			return result;
		}
		result.setData(shareFileInfo);

		// 增加访问次数
		FileShare fileShare = fileShareMapper.findByShareId(shareId);
		this.addAccessTimes(fileShare.getId());
		return result;
	}

	@Override
	public FileShare findByShareId(String shareId) {
		return fileShareMapper.findByShareId(shareId);
	}

	@Override
	public Result findAll(Page page) {

		Result result = new Result();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		PageHelper.startPage(page.getPageNum(), page.getPageSize());
		List<ShareFileShowList> list = fileShareMapper.findAllByUserId(user.getId(), page.getFileRealName());
		PageInfo<ShareFileShowList> pageInfo = new PageInfo<>(list);
		Map<String, Object> map = new HashMap<>();
		map.put("pageInfo", pageInfo);
		map.put("serverHost", appConfiguration.getCustomConfiguration().getServerHost());
		result.setData(map);

		return result;
		
	}

	@Override
	public Integer addAccessTimes(Integer id) {
		FileShare fileShare = fileShareMapper.selectByPrimaryKey(id);
		fileShare.setAccessTimes(fileShare.getAccessTimes() + 1);
		return fileShareMapper.updateByPrimaryKeySelective(fileShare);
	}

	@Override
	public Integer addDownloadTimes(Integer id) {
		FileShare shareFile = fileShareMapper.selectByPrimaryKey(id);
		shareFile.setDownloadTimes(shareFile.getDownloadTimes() + 1);
		return fileShareMapper.updateByPrimaryKeySelective(shareFile);
	}

	@Override
	public Integer addSaveTimes(Integer id) {
		FileShare shareFile = fileShareMapper.selectByPrimaryKey(id);
		shareFile.setSaveTimes(shareFile.getSaveTimes() + 1);
		return fileShareMapper.updateByPrimaryKeySelective(shareFile);
	}

	@Override
	public Result deleteShare(Integer id) {

		Result result = new Result();
		if (id == null) {
			return Result.paramIsNull(result);
		}
		FileShare shareFile = new FileShare();
		shareFile.setId(id);
		shareFile.setShareStatus(NetdiskConstant.SHARE_STATUS_OF_DEL);
		fileShareMapper.updateByPrimaryKeySelective(shareFile);

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

		FileShare shareFile = fileShareMapper.findByShareId(shareId);
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

		FileShare shareFile = fileShareMapper.findByShareId(shareId);
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
		UserFile userFile = null;
		if (fileId == null) {
			userFile = userFileMapper.selectByPrimaryKey(shareFile.getFileId());
		} else {
			// 检查fileId是否合法
			List<Integer> childIds = userFileMapper.findChildIds(shareFile.getFileId());
			if (!childIds.contains(fileId)) {
				result.setCode(NetdiskErrMsgConstant.SHARE_FILE_DOWNLOAD_WRONG);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_DOWNLOAD_WRONG));
				this.printlnWithResponse(response, result);
				return;
			}
			userFile = userFileMapper.selectByPrimaryKey(fileId);
		}
		FileOrigin fileOrigin = fileOriginMapper.findByUserFileId(userFile.getId());
		//增加下载次数
		this.addDownloadTimes(shareFile.getId());
		String filePath = fileOrigin.getFilePath();
		OutputStream out = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(fastDFSClientWrapper.getInputStream(filePath));
			// 设置响应头，控制浏览器下载该文件
			response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(userFile.getFileName(), "UTF-8"));
			// 设置文件大小
			response.addHeader("Content-Length", fileOrigin.getFileSize().toString());
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
	@Transactional
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

		FileShare fileShare = fileShareMapper.findByShareId(shareId);

		if (fileShare == null) {
			// 资源不存在
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			return result;
		}

		if (!fileShare.getSharePwd().equals(sharePwd)) {
			// 密码错误
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG));
			return result;
		}

		// 检查fileId是否合法
		if (fileId != null) {
			List<Integer> childIds = userFileMapper.findChildIds(fileShare.getFileId());
			if (!childIds.contains(fileId)) {
				result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_FAILED);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_FAILED));
				return result;
			}
		}

		// 要保存的文件或者是要保存的最外层文件夹
		UserFile fileForSave = null;
		if (fileId != null) {
			fileForSave = userFileMapper.selectByPrimaryKey(fileId);
		} else {
			fileForSave = userFileMapper.selectByPrimaryKey(fileShare.getFileId());
		}

		User user = (User) SecurityUtils.getSubject().getPrincipal();

		// 检查用户目录是否存在"我的资源"文件夹
		UserFile userFileCheck = new UserFile();
		userFileCheck.setUserId(user.getId());
		userFileCheck.setParentId(-1);
		userFileCheck.setFileName("我的资源");
		UserFile fileResources = userFileMapper.findFileByRealName(userFileCheck);
		if (fileResources == null) {
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

		if (fileForSave.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			// 检查目标父目录下是否存在同名文件夹
			userFileCheck = new UserFile();
			userFileCheck.setUserId(user.getId());
			userFileCheck.setParentId(fileResources.getId());
			userFileCheck.setFileName(fileForSave.getFileName());
			UserFile fileSearchResult = userFileMapper.findFileByRealName(userFileCheck);
			if (fileSearchResult != null) {
				// 目标目录存在同名文件夹，保存失败
				result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_TARGET_DIR_EXIST_SAME_NAME_DIR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_TARGET_DIR_EXIST_SAME_NAME_DIR));
				return result;
			}
		}

		// 查询剩余空间
		Map<String, Object> map = new HashMap<>();
		map.put("id", fileForSave.getId());
		map.put("sumsize", null);
		userFileMapper.getSumsizeDel(map);
		Long sumSize = null;
		if(map.get("sumsize") != null) {
			sumSize = Long.valueOf(map.get("sumsize") + "");
		}

		long totalMemory = user.getTotalMemory();
		long usedMemory = user.getUsedMemory();
		long availableMemory = totalMemory - usedMemory;
		if (availableMemory < sumSize) {

			//剩余空间不足
			result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH));
			return result;
		}
		usedMemory = usedMemory + sumSize;
		user.setUsedMemory(usedMemory);
		userMapper.updateByPrimaryKeySelective(user);

		// 递归保存文件夹内的文件到网盘
		Integer outterFileId = this.saveToCloudIteration(fileForSave.getId(), fileResources.getId(), user.getId(), fileShare);

		String targetPatentFilePath = userFileMapper.findPathname(fileResources.getId());
		String nowParentFilePath = "/";
		if (fileForSave.getParentId() != -1) {
			nowParentFilePath = userFileMapper.findPathname(fileForSave.getParentId());
		}

		// 更新filePath
		List<Integer> childIds = userFileMapper.findChildIds(outterFileId);
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

		result.setMsg("文件已成功保存到\"我的资源\"文件夹！");

		// 更新用户信息
		// userService.updatePrincipal();
		// 增加保存次数
		this.addSaveTimes(fileShare.getId());
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

		FileShare shareFile = fileShareMapper.findByShareId(shareId);

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
			List<Integer> childIds = userFileMapper.findChildIds(shareFile.getFileId());
			if (!childIds.contains(parentId)) {
				result.setCode(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_LIST_WRONG);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_LIST_WRONG));
				return result;
			}
		}

		UserFile param = new UserFile();
		if (parentId == null) {
			param.setId(shareFile.getFileId());
		} else {
			param.setUserId(shareFile.getShareUserId());
			param.setParentId(parentId);
		}
		List<FileShowInfo> files = userFileMapper.findAllFile(param, null, null);
		Map<String, Object> map = new HashMap<>();
		map.put("nginxServer", appConfiguration.getFdfsNginxServer());
		map.put("files", files);
		result.setData(map);

		return result;
	}

	/**
	 * 获取分享文件媒体信息
	 * @param shareId
	 * @param sharePwd
	 * @param fileId
	 * @return
	 */
	@Override
	public Result getFileMediaInfo(String shareId, String sharePwd, Integer fileId) {

		Result result = new Result();
		if (shareId == null || "".equals(shareId) || sharePwd == null || "".equals(sharePwd)) {
			return Result.paramIsNull(result);
		}

		FileShare fileShare = fileShareMapper.findByShareId(shareId);
		if (fileShare == null) {
			// 资源不存在
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			return result;
		}
		if (!fileShare.getSharePwd().equals(sharePwd)) {
			// 密码错误
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG));
			return result;
		}
		if (fileShare.getFileId().intValue() != fileId) {
			// 检查fileId是否合法
			List<Integer> childIds = userFileMapper.findChildIds(fileShare.getFileId());
			if (!childIds.contains(fileId)) {
				result.setCode(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_MEDIA_INFO_WRONG);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_MEDIA_INFO_WRONG));
				return result;
			}
		}
		UserFile userFile = userFileMapper.selectByPrimaryKey(fileId);
		FileShowMedia fileMediaInfo = fileMediaMapper.getFileMediaInfo(userFile.getKey());
		result.setData(fileMediaInfo);
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
	 * @param fileShare 分享文件
	 * @return 保存后的分享文件，最外层文件夹（文件）id
	 */
	public Integer saveToCloudIteration(Integer oldParentId, Integer newParentId, Integer userId, FileShare fileShare) {

		UserFile userFile = userFileMapper.selectByPrimaryKey(oldParentId);
		userFile.setId(null);
		userFile.setKey(FileUtil.makeFileKey());
		userFile.setCreateTime(new Date());
		userFile.setUpdateTime(new Date());
		userFile.setUserId(userId);
		userFile.setParentId(newParentId);
		userFileMapper.insertSelective(userFile);

		if (userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			// 为文件夹，继续递归
			UserFile fileSearch = new UserFile();
			fileSearch.setParentId(oldParentId);
			fileSearch.setUserId(fileShare.getShareUserId());
			List<FileShowInfo> files = userFileMapper.findAllFile(fileSearch, null, null);
			if (files != null && files.size() > 0) {
				for (FileShowInfo f : files) {
					this.saveToCloudIteration(f.getId(), userFile.getId(), userId, fileShare);
				}
			}
		}

		return userFile.getId();
	}
}
