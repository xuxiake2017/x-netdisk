package group.xuxiake.web.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import group.xuxiake.common.entity.*;
import group.xuxiake.common.entity.param.ShareFileParam;
import group.xuxiake.common.entity.show.FileShowInfo;
import group.xuxiake.common.entity.show.FileShowMedia;
import group.xuxiake.common.mapper.*;
import group.xuxiake.common.util.FileUtil;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.common.entity.show.ShareFileShowInfo;
import group.xuxiake.common.entity.show.ShareFileShowList;
import group.xuxiake.web.service.ShareFileService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
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
	@Autowired
	private HttpServletRequest request;
	@Resource
	private UserMapper userMapper;
	@Resource
	private AppConfiguration appConfiguration;
	@Resource
	private FileMediaMapper fileMediaMapper;
	@Resource
	private FileOriginMapper fileOriginMapper;
	@Resource
	private RedisUtils redisUtils;
	@Resource
	private FileDownloadUtil fileDownloadUtil;

	private static class ShareAccess {
		private final FileShare share;
		private final UserFile root;
		private final Result error;

		private ShareAccess(FileShare share, UserFile root, Result error) {
			this.share = share;
			this.root = root;
			this.error = error;
		}
	}

	private Integer getCurrentUserId() {
		User principal = (User) SecurityUtils.getSubject().getPrincipal();
		return principal == null ? null : principal.getId();
	}

	private Result forbidden() {
		Result result = new Result();
		result.setCode(NetdiskErrMsgConstant.FORBIDDEN);
		return result;
	}

	private Result error(int code) {
		Result result = new Result();
		result.setCode(code);
		return result;
	}

	private ShareAccess loadShare(String shareId, String sharePwd, boolean verifyPassword) {
		if (shareId == null || shareId.trim().isEmpty()) {
			return new ShareAccess(null, null, Result.paramIsNull(new Result()));
		}
		FileShare share = fileShareMapper.findByShareId(shareId.trim());
		if (share == null) {
			return new ShareAccess(null, null, error(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
		}
		if (share.getOverTime() != null && !share.getOverTime().after(new Date())) {
			return new ShareAccess(null, null, error(NetdiskErrMsgConstant.GET_SHARE_FILE_EXPIRED));
		}
		UserFile root = userFileMapper.selectByPrimaryKey(share.getFileId());
		if (root == null || !Integer.valueOf(NetdiskConstant.FILE_STATUS_OF_NORMAL).equals(root.getStatus())
				|| !share.getShareUserId().equals(root.getUserId())) {
			return new ShareAccess(null, null, error(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
		}
		if (verifyPassword && !Integer.valueOf(1).equals(share.getIsPublic())) {
			if (sharePwd == null || sharePwd.isEmpty()) {
				return new ShareAccess(null, null, Result.paramIsNull(new Result()));
			}
			String attemptsKey = passwordAttemptsKey(share.getShareId());
			Object attemptsValue = redisUtils.get(attemptsKey);
			long attempts = attemptsValue instanceof Number ? ((Number) attemptsValue).longValue() : 0L;
			int maxAttempts = appConfiguration.getCustomConfiguration().getShareMaxPasswordAttempts();
			if (attempts >= maxAttempts) {
				return new ShareAccess(null, null, error(NetdiskErrMsgConstant.SHARE_PWD_ATTEMPTS_EXCEEDED));
			}
			if (!Objects.equals(share.getSharePwd(), sharePwd)) {
				attempts = redisUtils.incr(attemptsKey, 1L);
				if (attempts == 1L) {
					long seconds = Math.max(1L,
							appConfiguration.getCustomConfiguration().getSharePasswordLockTime() / 1000L);
					redisUtils.expire(attemptsKey, seconds);
				}
				int code = attempts >= maxAttempts
						? NetdiskErrMsgConstant.SHARE_PWD_ATTEMPTS_EXCEEDED
						: NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG;
				return new ShareAccess(null, null, error(code));
			}
			redisUtils.del(attemptsKey);
		}
		return new ShareAccess(share, root, null);
	}

	private String passwordAttemptsKey(String shareId) {
		return "SHARE_PWD_ATTEMPTS:" + shareId + ":" + request.getRemoteAddr();
	}

	private UserFile resolveSharedFile(ShareAccess access, Integer fileId) {
		if (fileId == null || fileId.equals(access.root.getId())) {
			return access.root;
		}
		List<Integer> childIds = userFileMapper.findChildIds(access.root.getId());
		if (!childIds.contains(fileId)) {
			return null;
		}
		UserFile file = userFileMapper.selectByPrimaryKey(fileId);
		return file != null
				&& Integer.valueOf(NetdiskConstant.FILE_STATUS_OF_NORMAL).equals(file.getStatus())
				&& access.share.getShareUserId().equals(file.getUserId()) ? file : null;
	}

	/**
	 * 创建分享文件
	 * @param param
	 * @return
	 */
	@Transactional
	@Override
	public Result shareFile(ShareFileParam param) {

		Result result = new Result();
		if (param == null || param.getFileKey() == null || param.getFileKey().trim().isEmpty()
				|| (param.getIsPublic() != null && param.getIsPublic() != 0 && param.getIsPublic() != 1)
				|| (param.getExpireDays() != null && (param.getExpireDays() < 0 || param.getExpireDays() > 365))) {
			return Result.paramIsNull(result);
		}

		UserFile fileUpload = userFileMapper.findFileByKey(param.getFileKey());
		Integer currentUserId = getCurrentUserId();
		if (fileUpload == null || currentUserId == null || !currentUserId.equals(fileUpload.getUserId())) {
			return forbidden();
		}
		Integer shareUserId = fileUpload.getUserId();
		Date shareTime = new Date();
		FileShare fileShare = new FileShare();
		boolean publicShare = Integer.valueOf(1).equals(param.getIsPublic());
		fileShare.setSharePwd(publicShare ? null : RandomCodeUtil.getRandomSharePwd());
		fileShare.setIsPublic(publicShare ? 1 : 0);
		fileShare.setShareUserId(shareUserId);
		fileShare.setShareTime(shareTime);
		if (param.getExpireDays() != null && param.getExpireDays() > 0) {
			fileShare.setOverTime(new Date(shareTime.getTime() + param.getExpireDays() * 86400000L));
		}
		fileShare.setShareStatus(NetdiskConstant.SHARE_STATUS_OF_NORMAL);
		fileShare.setFileId(fileUpload.getId());
		fileShare.setAccessTimes(0);
		fileShare.setDownloadTimes(0);
		fileShare.setSaveTimes(0);

		boolean inserted = false;
		for (int i = 0; i < 10 && !inserted; i++) {
			fileShare.setShareId(RandomCodeUtil.getRandomShareId());
			try {
				fileShareMapper.insertSelective(fileShare);
				inserted = true;
			} catch (DuplicateKeyException ignored) {
				fileShare.setId(null);
			}
		}
		if (!inserted) {
			throw new IllegalStateException("生成唯一分享 ID 失败");
		}

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

		ShareAccess access = loadShare(shareId, null, false);
		if (access.error != null) {
			return access.error;
		}
		Result result = new Result();
		ShareFileShowInfo shareFileInfo = fileShareMapper.getShareFileInfo(shareId.trim());
		if (shareFileInfo == null) {
			result.setCode(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST));
			return result;
		}
		result.setData(shareFileInfo);

		// 增加访问次数
		this.addAccessTimes(access.share.getId());
		return result;
	}

	@Override
	public FileShare findByShareId(String shareId) {
		ShareAccess access = loadShare(shareId, null, false);
		return access.error == null ? access.share : null;
	}

	@Override
	public Result findAll(Page page) {

		Result result = new Result();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		if (user == null) {
			result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
			return result;
		}
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
		return fileShareMapper.incrementAccessTimes(id);
	}

	@Override
	public Integer addDownloadTimes(Integer id) {
		return fileShareMapper.incrementDownloadTimes(id);
	}

	@Override
	public Integer addSaveTimes(Integer id) {
		return fileShareMapper.incrementSaveTimes(id);
	}

	@Override
	public Result deleteShare(Integer id) {

		Result result = new Result();
		if (id == null) {
			return Result.paramIsNull(result);
		}
		FileShare shareFile = fileShareMapper.selectByPrimaryKey(id);
		Integer currentUserId = getCurrentUserId();
		if (shareFile == null || currentUserId == null || !currentUserId.equals(shareFile.getShareUserId())) {
			return forbidden();
		}
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
		ShareAccess access = loadShare(shareId, sharePwd, true);
		return access.error == null ? new Result() : access.error;
	}

	/**
	 * 分享文件下载
	 * @param shareId
	 * @param sharePwd
	 */
	@Override
	public void download(String shareId, String sharePwd, Integer fileId) {
		ShareAccess access = loadShare(shareId, sharePwd, true);
		if (access.error != null) {
			this.printlnWithResponse(response, access.error);
			return;
		}
		UserFile userFile = resolveSharedFile(access, fileId);
		if (userFile == null) {
			this.printlnWithResponse(response, error(NetdiskErrMsgConstant.SHARE_FILE_DOWNLOAD_WRONG));
			return;
		}
		try {
			fileDownloadUtil.download(userFile, response);
			this.addDownloadTimes(access.share.getId());
		} catch (Exception e) {
			log.error("文件下载失败", e);
			if (!response.isCommitted()) {
				this.printlnWithResponse(response, error(NetdiskErrMsgConstant.EXCEPTION));
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

		ShareAccess access = loadShare(shareId, sharePwd, true);
		if (access.error != null) {
			return access.error;
		}
		FileShare fileShare = access.share;
		UserFile fileForSave = resolveSharedFile(access, fileId);
		if (fileForSave == null) {
			return error(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_FAILED);
		}

		User principal = (User) SecurityUtils.getSubject().getPrincipal();
		User user = userMapper.selectByPrimaryKeyForUpdate(principal.getId());
		if (userFileMapper.countNormalFilesWithoutOrigin(user.getId()) > 0) {
			result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
			result.setMsg("当前账号存在文件存储信息异常，暂时无法转存");
			return result;
		}

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
		long sumSize = map.get("sumsize") == null ? 0L : ((Number) map.get("sumsize")).longValue();

		long totalMemory = user.getTotalMemory();
		Long currentUsedMemory = userFileMapper.sumNormalFileSizeByUserId(user.getId());
		long usedMemory = currentUsedMemory == null ? 0L : currentUsedMemory;
		user.setUsedMemory(usedMemory);
		userMapper.updateByPrimaryKeySelective(user);
		long availableMemory = totalMemory - usedMemory;
		if (availableMemory < sumSize) {

			//剩余空间不足
			result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH));
			return result;
		}
		// 递归保存文件夹内的文件到网盘
		this.saveToCloudIteration(fileForSave.getId(), fileResources.getId(), user.getId(), fileShare);

		// 源目录可能在统计后发生变化，以实际复制结果校准容量并再次校验配额。
		Long copiedUsedMemory = userFileMapper.sumNormalFileSizeByUserId(user.getId());
		long actualUsedMemory = copiedUsedMemory == null ? 0L : copiedUsedMemory;
		if (actualUsedMemory > totalMemory) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			result.setCode(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH);
			return result;
		}
		user.setUsedMemory(actualUsedMemory);
		userMapper.updateByPrimaryKeySelective(user);

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

		ShareAccess access = loadShare(shareId, sharePwd, true);
		if (access.error != null) {
			return access.error;
		}
		Result result = new Result();
		if (parentId != null) {
			UserFile parent = resolveSharedFile(access, parentId);
			if (parent == null || !Integer.valueOf(NetdiskConstant.FILE_IS_DIR).equals(parent.getIsDir())) {
				return error(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_LIST_WRONG);
			}
		}

		UserFile param = new UserFile();
		if (parentId == null) {
			param.setId(access.share.getFileId());
		} else {
			param.setUserId(access.share.getShareUserId());
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

		ShareAccess access = loadShare(shareId, sharePwd, true);
		if (access.error != null) {
			return access.error;
		}
		Result result = new Result();
		UserFile userFile = resolveSharedFile(access, fileId);
		if (userFile == null || Integer.valueOf(NetdiskConstant.FILE_IS_DIR).equals(userFile.getIsDir())) {
			return error(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_MEDIA_INFO_WRONG);
		}
		FileShowMedia fileMediaInfo = fileMediaMapper.getFileMediaInfo(userFile.getKey());
		result.setData(fileMediaInfo);
		return result;
	}

	public void printlnWithResponse(HttpServletResponse response, Result result) {

		if (!response.isCommitted()) {
			response.reset();
		}
		if (NetdiskErrMsgConstant.EXCEPTION == result.getCode()) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} else if (NetdiskErrMsgConstant.PARAM_IS_NULL == result.getCode()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else if (NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST == result.getCode()
				|| NetdiskErrMsgConstant.GET_SHARE_FILE_EXPIRED == result.getCode()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else if (NetdiskErrMsgConstant.SHARE_PWD_ATTEMPTS_EXCEEDED == result.getCode()) {
			response.setStatus(429);
		} else {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
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
	 */
	private void saveToCloudIteration(Integer oldParentId, Integer newParentId, Integer userId, FileShare fileShare) {

		UserFile userFile = userFileMapper.selectByPrimaryKey(oldParentId);
		if (userFile == null) {
			throw new IllegalStateException("分享源文件不存在");
		}
		List<FileShowInfo> sourceChildren = Collections.emptyList();
		if (userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			UserFile fileSearch = new UserFile();
			fileSearch.setParentId(oldParentId);
			fileSearch.setUserId(fileShare.getShareUserId());
			sourceChildren = userFileMapper.findAllFile(fileSearch, null, null);
		}
		if (Integer.valueOf(NetdiskConstant.FILE_IS_NOT_DIR).equals(userFile.getIsDir())) {
			if (userFile.getOriginId() == null
					|| fileOriginMapper.selectByPrimaryKeyForUpdate(userFile.getOriginId()) == null) {
				throw new IllegalStateException("分享源文件存储信息不存在");
			}
		}
		userFile.setId(null);
		userFile.setKey(FileUtil.makeFileKey());
		userFile.setCreateTime(new Date());
		userFile.setUpdateTime(new Date());
		userFile.setUserId(userId);
		userFile.setParentId(newParentId);
		userFileMapper.insertSelective(userFile);

		if (userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			// 使用插入副本前的源目录快照，避免同一用户保存祖先目录时递归到新副本。
			if (sourceChildren != null && !sourceChildren.isEmpty()) {
				for (FileShowInfo f : sourceChildren) {
					this.saveToCloudIteration(f.getId(), userFile.getId(), userId, fileShare);
				}
			}
		}

	}
}
