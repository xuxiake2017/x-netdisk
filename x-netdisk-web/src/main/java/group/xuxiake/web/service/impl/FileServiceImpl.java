package group.xuxiake.web.service.impl;

import com.drew.imaging.ImageProcessingException;
import group.xuxiake.common.entity.MP3Info;
import group.xuxiake.common.entity.*;
import group.xuxiake.common.entity.param.FileUploadParamByMD5;
import group.xuxiake.common.entity.show.FileShowMedia;
import group.xuxiake.common.mapper.*;
import group.xuxiake.common.util.FileUtil;
import group.xuxiake.common.util.MP3Utils;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.configuration.CustomConfiguration;
import group.xuxiake.common.entity.show.FilePathStore;
import group.xuxiake.web.handler.LowKbpsHandler;
import group.xuxiake.web.handler.VideoTransformHandler;
import group.xuxiake.web.service.FileService;
import group.xuxiake.web.service.RouteService;
import group.xuxiake.web.service.UserService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

@Slf4j
@Service("fileService")
public class FileServiceImpl implements FileService {

	@Resource
	private UserService userService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Resource
    private FastDFSClientWrapper fastDFSClientWrapper;
	@Resource
    private AppConfiguration appConfiguration;
	@Resource
	private RouteService routeService;
	@Resource
	private FileOriginMapper fileOriginMapper;
	@Resource
	private UserFileMapper userFileMapper;
	@Resource
	private FileMediaMapper fileMediaMapper;
	@Resource
	private UserMapper userMapper;
	@Resource
	private FileRecycleMapper fileRecycleMapper;
	@Resource
	private DocConverterUtil docConverterUtil;
	@Resource(name = "mediaTaskExecutor")
	private TaskExecutor mediaTaskExecutor;

	private Integer getCurrentUserId() {
		User principal = (User) SecurityUtils.getSubject().getPrincipal();
		return principal == null ? null : principal.getId();
	}

	private UserFile findOwnedFileByKey(String fileKey) {
		Integer userId = getCurrentUserId();
		UserFile userFile = StringUtils.isEmpty(fileKey) ? null : userFileMapper.findFileByKey(fileKey);
		return userFile != null && userId != null && userId.equals(userFile.getUserId()) ? userFile : null;
	}

	private UserFile findOwnedFileById(Integer fileId) {
		Integer userId = getCurrentUserId();
		UserFile userFile = fileId == null ? null : userFileMapper.selectByPrimaryKey(fileId);
		return userFile != null && userId != null && userId.equals(userFile.getUserId()) ? userFile : null;
	}

	private boolean isOwnedDirectory(Integer parentId) {
		if (parentId == null) {
			return false;
		}
		if (parentId == -1) {
			return true;
		}
		UserFile parent = findOwnedFileById(parentId);
		return parent != null && Objects.equals(parent.getIsDir(), NetdiskConstant.FILE_IS_DIR);
	}

	private Result forbidden() {
		Result result = new Result();
		result.setCode(NetdiskErrMsgConstant.FORBIDDEN);
		return result;
	}

	private boolean isInvalidFileName(String fileName) {
		if (StringUtils.isBlank(fileName) || fileName.length() > 255
				|| ".".equals(fileName) || "..".equals(fileName)
				|| fileName.indexOf('/') >= 0 || fileName.indexOf('\\') >= 0) {
			return true;
		}
		for (int i = 0; i < fileName.length(); i++) {
			if (Character.isISOControl(fileName.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	private void scheduleMediaTask(Runnable task, String[] cachePaths) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			submitMediaTask(task, cachePaths);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				submitMediaTask(task, cachePaths);
			}

			@Override
			public void afterCompletion(int status) {
				if (status != TransactionSynchronization.STATUS_COMMITTED) {
					deleteCacheFiles(cachePaths);
				}
			}
		});
	}

	private void submitMediaTask(Runnable task, String[] cachePaths) {
		try {
			mediaTaskExecutor.execute(task);
		} catch (RuntimeException e) {
			deleteCacheFiles(cachePaths);
			throw e;
		}
	}

	private void deleteCacheFiles(String[] cachePaths) {
		for (String path : cachePaths) {
			if (path != null) {
				FileUtils.deleteQuietly(new File(path));
			}
		}
	}

	/**
	 * 检查文件MD5值是否在数据库存在
	 * @param md5Hex
	 * @return
	 */
	@Override
	public Result checkMd5(String md5Hex) {
		Result result = new Result();
		// 根据前端传来的文件MD5值去数据库查找
		FileOrigin fileOrigin = fileOriginMapper.findFileByMd5Hex(md5Hex);
		if (fileOrigin == null) {
			//服务器不存在该md5值
			result.setData(NetdiskErrMsgConstant.CHECKMD5_MD5_NOT_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_MD5_NOT_EXIST));
		} else {
			//服务器存在该md5值
			result.setData(NetdiskErrMsgConstant.CHECKMD5_MD5_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_MD5_EXIST));
		}
		return result;
	}

	/**
	 * 文件MD5值在数据库存在，假上传
	 * @param param
	 * @return
	 */
	@Transactional
	@Override
	public Result uploadMD5(FileUploadParamByMD5 param) {

		Result result = new Result();
		User userThreadContext = (User) SecurityUtils.getSubject().getPrincipal();
		User user = userMapper.selectByPrimaryKeyForUpdate(userThreadContext.getId());

		CustomConfiguration customConfiguration = appConfiguration.getCustomConfiguration();
		FileOrigin fileOrigin = fileOriginMapper.findFileByMd5Hex(param.getMd5Hex());
		if (fileOrigin == null) {
			result.setCode(NetdiskErrMsgConstant.CHECKMD5_MD5_NOT_EXIST);
			return result;
		}
		long fileSize = fileOrigin.getFileSize();
		if (!isOwnedDirectory(param.getParentId())) {
			return forbidden();
		}

        // 检查文件大小
		try {
			// 普通用户
			if (user.getUserStatus() == NetdiskConstant.USER_STATUS_NORMAL) {
				if (fileSize > customConfiguration.getFileSizeMax()) {
					result.setCode(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT);
					result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT));
					return result;
				}
			}
			// vip用户
			if (user.getUserStatus() == NetdiskConstant.USER_STATUS_VIP) {
				if (fileSize > customConfiguration.getVipFileSizeMax()) {
					result.setCode(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT);
					result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT));
					return result;
				}
			}

			// 查询剩余空间
			long totalMemory = user.getTotalMemory();
			long usedMemory = user.getUsedMemory();
			long availableMemory = totalMemory - usedMemory;
			if (availableMemory < fileSize) {
				result.setCode(NetdiskErrMsgConstant.CHECKMD5_AVAIABLE_MEMORY_NOT_ENOUGH);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_AVAIABLE_MEMORY_NOT_ENOUGH));
				return result;
			}

			usedMemory = usedMemory + fileSize;
			user.setUsedMemory(usedMemory);
			userMapper.updateByPrimaryKey(user);

			String fileKey = FileUtil.makeFileKey();
			UserFile userFile = new UserFile();
			userFile.setOriginId(fileOrigin.getId());
			userFile.setFileName(param.getFileRealName());
			userFile.setIsDir(NetdiskConstant.FILE_IS_NOT_DIR);
			userFile.setUserId(user.getId());
			userFile.setParentId(param.getParentId());
			userFile.setKey(fileKey);
			userFile.setStatus(NetdiskConstant.FILE_STATUS_OF_NORMAL);
			userFile.setCreateTime(new Date());
			userFile.setUpdateTime(new Date());
			userFileMapper.insertSelective(userFile);

			// userService.updatePrincipal();
			return result;
		} catch (Exception e) {
			log.error("检查md5失败", e);
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		result.setCode(NetdiskErrMsgConstant.CHECKMD5_FAILED);
		result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_FAILED));
		return result;
	}

	/**
	 * 列出所有文件夹，用于文件移动
	 * @param parentId
	 * @return
	 */
	@Override
	public Result listAllDir(Integer parentId) {
		if(parentId == null ) {
			parentId = -1;
		}
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		List<UserFile> fileList = userFileMapper.findAllDir(user.getId(), parentId);
		Result result = new Result();
		result.setData(fileList);
		return result;
	}

	/**
	 * 文件重命名
	 * @param param
	 * @return
	 */
	@Transactional
	@Override
	public Result reName(UserFile param) {
		Result result = new Result();
		if (param == null || StringUtils.isBlank(param.getKey()) || isInvalidFileName(param.getFileName())) {
			return Result.paramIsNull(result);
		}
		UserFile ownedFile = findOwnedFileByKey(param.getKey());
		if (ownedFile == null) {
			return forbidden();
		}
		param.setUserId(ownedFile.getUserId());
		param.setParentId(ownedFile.getParentId());
		param.setIsDir(ownedFile.getIsDir());

		if (param.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			UserFile userFile = ownedFile;
			if (param.getFileName().equals(userFile.getFileName())) {
				return result;
			}
			//是文件夹，要判断当前目录下是否有同名文件夹
			UserFile userFileByRealName = userFileMapper.findFileByRealName(param);
			if (userFileByRealName != null) {
				//该目录下有同名文件夹
				result.setCode(NetdiskErrMsgConstant.RENAME_FILE_NAME_EXIST);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.RENAME_FILE_NAME_EXIST));
				return result;
			}
		}
		UserFile update = new UserFile();
		update.setKey(ownedFile.getKey());
		update.setFileName(param.getFileName());
		update.setUpdateTime(new Date());
		userFileMapper.updateByKeySelective(update);
		return result;
	}

	/**
	 * 文件上传
	 * @param parentId
	 * @param md5Hex
	 * @param lastModifiedDate
	 * @param fileRealName
	 * @param file
	 * @return
	 */
	@Transactional
	@Override
	public Result fileUpload(Integer parentId, String md5Hex, Long lastModifiedDate, String fileRealName, MultipartFile file) {
		// 消息提示
		Result result = new Result();
        try {

			User userThreadContext = (User) SecurityUtils.getSubject().getPrincipal();
			User user = userMapper.selectByPrimaryKeyForUpdate(userThreadContext.getId());

			if (file == null || file.isEmpty()) {
				result.setCode(NetdiskErrMsgConstant.UPLOAD_FILE_IS_NULL);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UPLOAD_FILE_IS_NULL));
				return result;
			}
			if (StringUtils.isEmpty(fileRealName)) {
				// 得到上传的文件名称，
				fileRealName = file.getOriginalFilename();
			}
			// 得到上传文件的扩展名
			String fileExtName = "";
			if (fileRealName.contains(".")) {
				fileExtName = fileRealName.substring(fileRealName.lastIndexOf(".") + 1);
			}
			//文件大小
			long fileSize = file.getSize();
			CustomConfiguration customConfiguration = appConfiguration.getCustomConfiguration();
			long maxFileSize = user.getUserStatus() == NetdiskConstant.USER_STATUS_VIP
					? customConfiguration.getVipFileSizeMax() : customConfiguration.getFileSizeMax();
			if (fileSize > maxFileSize) {
				result.setCode(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT);
				return result;
			}
			// 查询剩余空间
			long totalMemory = user.getTotalMemory();
			long usedMemory = user.getUsedMemory();
			long availableMemory = totalMemory - usedMemory;
			if (availableMemory < fileSize) {
				result.setCode(NetdiskErrMsgConstant.UPLOAD_AVAIABLE_MEMORY_NOT_ENOUGH);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UPLOAD_AVAIABLE_MEMORY_NOT_ENOUGH));
				return result;
			}
			//根据文件后缀判断文件类型
			Integer fileType = FileUtil.getFileType(fileExtName.toLowerCase());
			if (parentId == null) {
				parentId = -1;
			}
			if (!isOwnedDirectory(parentId)) {
				return forbidden();
			}
			String filePath = fastDFSClientWrapper.uploadFile(file);
			String fileKey = FileUtil.makeFileKey();

			FileOrigin fileOrigin = new FileOrigin();
			fileOrigin.setFilePath(filePath);
			fileOrigin.setFileRealName(fileRealName);
			fileOrigin.setFileExtName(fileExtName);
			fileOrigin.setFileSize(fileSize);
			fileOrigin.setFileType(fileType);
			fileOrigin.setUserId(user.getId());
			fileOrigin.setMd5Hex(md5Hex);
			fileOrigin.setFileStatus(NetdiskConstant.DATA_NORMAL_STATUS);
			fileOrigin.setCreateTime(new Date());
			fileOrigin.setUpdateTime(new Date());

			fileOriginMapper.insertSelective(fileOrigin);

			UserFile userFile = new UserFile();
			userFile.setOriginId(fileOrigin.getId());
			userFile.setFileName(fileRealName);
			userFile.setIsDir(NetdiskConstant.FILE_IS_NOT_DIR);
			userFile.setUserId(user.getId());
			userFile.setParentId(parentId);
			userFile.setKey(fileKey);
			userFile.setStatus(NetdiskConstant.FILE_STATUS_OF_NORMAL);
			userFile.setCreateTime(new Date());
			userFile.setUpdateTime(new Date());

			userFileMapper.insertSelective(userFile);

			this.handleMediaFile(fileOrigin, lastModifiedDate);

			usedMemory = usedMemory + fileSize;
			user.setUsedMemory(usedMemory);
			userMapper.updateByPrimaryKey(user);

		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			result.setCode(NetdiskErrMsgConstant.UPLOAD_TIMEOUT);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UPLOAD_TIMEOUT));
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return result;
		} catch (Exception e) {
			log.error("上传文件失败", e);
			result.setCode(NetdiskErrMsgConstant.UPLOAD_FAILED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UPLOAD_FAILED));
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return result;
		}
		// userService.updatePrincipal();

		return result;
	}

	/**
	 * 处理媒体文件、转码
	 * @param fileOrigin
	 * @throws IOException
	 */
	public void handleMediaFile(FileOrigin fileOrigin, Long lastModifiedDate) throws IOException {

		// 获取系统临时文件夹
		String cacheParentPath = System.getProperty("java.io.tmpdir");
		cacheParentPath = cacheParentPath.replaceAll("\\\\", "/");
		String filePath = fileOrigin.getFilePath();
		String fdfsNginxServer = appConfiguration.getFdfsNginxServer();

		FileMedia fileMedia = new FileMedia();
		fileMedia.setOriginId(fileOrigin.getId());

		if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_WORD
				|| fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_EXCEL
				|| fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_POWERPOINT
				|| fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_TXT) {

			byte[] bytes = fastDFSClientWrapper.downloadFile(filePath, inputStream -> {
				if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_TXT) {
					byte[] converted = FileUtil.textEncodingConvert(IOUtils.toByteArray(inputStream));
					try (ByteArrayInputStream convertedInput = new ByteArrayInputStream(converted)) {
						return docConverterUtil.docToPDF(convertedInput, fileOrigin.getFileExtName());
					}
				}
				return docConverterUtil.docToPDF(inputStream, fileOrigin.getFileExtName());
			});
			if (bytes != null) {
				try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
				String previewUrl = fastDFSClientWrapper.uploadFile(inputStream, bytes.length, "pdf");
				previewUrl = fdfsNginxServer + "/" + previewUrl;
				fileOrigin.setPreviewUrl(previewUrl);
				fileOriginMapper.updateByPrimaryKeySelective(fileOrigin);
				}
			}
		}

		if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_PDF) {

			String previewUrl = fdfsNginxServer + "/" + fileOrigin.getFilePath();
			fileOrigin.setPreviewUrl(previewUrl);
			fileOriginMapper.updateByPrimaryKeySelective(fileOrigin);
		}

		//获得图片的信息（拍摄时间、宽、高）
		if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_PIC) {
			try {
				Map<String, Object> imgInfo = fastDFSClientWrapper.downloadFile(filePath, inputStream -> {
					try {
						return ImageUtil.getImgInfo(inputStream, lastModifiedDate);
					} catch (ImageProcessingException e) {
						throw new IOException(e);
					}
				});
				fileMedia.setShootTime((Date) imgInfo.get("shootTime"));
				fileMedia.setImgHeight((Integer) imgInfo.get("imgHeight"));
				fileMedia.setImgWidth((Integer) imgInfo.get("imgWidth"));
			} catch (Exception e) {
				fileMedia.setShootTime(new Date(lastModifiedDate));
				fileMedia.setImgHeight(1000);
				fileMedia.setImgWidth(1000);
			}
			String previewUrl = filePath;
			ByteArrayOutputStream byteArrayOutputStream = null;
			ByteArrayInputStream inputStream = null;
			try {
				//如果图片大小超过100kb，对图片进行缩略，否则不缩略
				if (fileOrigin.getFileSize() >= 100 * 1024) {
					byte[] bytes = fastDFSClientWrapper.downloadFile(filePath, inputStreamFromStorage -> {
						try (ByteArrayOutputStream thumbnailOutput = new ByteArrayOutputStream()) {
							Thumbnails.of(inputStreamFromStorage).size(1000, 1000).outputQuality(0.5D)
									.toOutputStream(thumbnailOutput);
							return thumbnailOutput.toByteArray();
						}
					});
					inputStream = new ByteArrayInputStream(bytes);
					previewUrl = fastDFSClientWrapper.uploadFile(inputStream, bytes.length, fileOrigin.getFileExtName());
				} else {
					previewUrl = filePath;
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				previewUrl = filePath;
			} finally {
				try {
					if (byteArrayOutputStream != null) {
						byteArrayOutputStream.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			previewUrl = fdfsNginxServer + "/" + previewUrl;
			fileOrigin.setPreviewUrl(previewUrl);
			fileOriginMapper.updateByPrimaryKeySelective(fileOrigin);
			fileMedia.setThumbnailUrl(previewUrl);
		}
		// 对于上传上来的视频，开辟线程进行转码，方便在线播放
		if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_VIDEO) {

			String cachePathBefore = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName(fileOrigin.getFileExtName()));
			String cachePathMiddle = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName("mp4"));
			String cachePathAfter = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName("mp4"));
			String thumbnailCachePath = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName("jpg"));
			String[] paths = {cachePathBefore, cachePathMiddle, cachePathAfter, thumbnailCachePath};
			scheduleMediaTask(new VideoTransformHandler(fileOriginMapper, fileMediaMapper,
					fastDFSClientWrapper, paths, fileOrigin, fdfsNginxServer, fileMedia), paths);
		}
		if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_MUSIC &&  "mp3".equals(fileOrigin.getFileExtName())) {
			// lowKmps，降低mp3码率

			String cachePathBefore = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName(fileOrigin.getFileExtName()));
			String cachePathAfter = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName(fileOrigin.getFileExtName()));

			fastDFSClientWrapper.downloadFile(filePath, new File(cachePathBefore));
			String[] paths = {cachePathBefore,cachePathAfter};

			ByteArrayOutputStream byteArrayOutputStream = null;
			ByteArrayInputStream byteArrayInputStream = null;
			String musicPoster = "";
			try {
				MP3Info mp3Info = MP3Utils.getMP3Info(paths[0]);
				if (mp3Info.getArtist() != null) {
					fileMedia.setMusicArtist(mp3Info.getArtist());
				} else {
					fileMedia.setMusicArtist("未知歌手");
				}
				if (mp3Info.getAlbumImage() != null) {
					if (mp3Info.getAlbumImage().length > 1024 * 100) { // 封面大于100KB要进行缩略
						byteArrayInputStream = new ByteArrayInputStream(mp3Info.getAlbumImage());
						byteArrayOutputStream = new ByteArrayOutputStream();
						Thumbnails.of(byteArrayInputStream).size(1000, 1000).outputQuality(0.5D).toOutputStream(byteArrayOutputStream);
						byte[] bytes = byteArrayOutputStream.toByteArray();
						byteArrayInputStream = new ByteArrayInputStream(bytes);
						musicPoster = fastDFSClientWrapper.uploadFile(byteArrayInputStream, bytes.length, "jpg");
					} else {
						byteArrayInputStream = new ByteArrayInputStream(mp3Info.getAlbumImage());
						musicPoster = fastDFSClientWrapper.uploadFile(byteArrayInputStream, mp3Info.getAlbumImage().length, "jpg");
					}
					if (!StringUtils.isEmpty(musicPoster)) {
						musicPoster = fdfsNginxServer + "/" + musicPoster;
						fileMedia.setMusicPoster(musicPoster);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				try {
					if (byteArrayOutputStream != null) {
						byteArrayOutputStream.close();
					}
					if (byteArrayInputStream != null) {
						byteArrayInputStream.close();
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			//System.out.println("[音频开始降低码率]");
			scheduleMediaTask(new LowKbpsHandler(fileOriginMapper, fastDFSClientWrapper, paths,
					fileOrigin, fdfsNginxServer), paths);
		}
		fileMediaMapper.insertSelective(fileMedia);
	}

	/**
	 * 创建文件夹
	 * @param param
	 * @return
	 */
	@Transactional
	@Override
	public Result mkDir(UserFile param) {

		Result result = new Result();
		User user = (User) SecurityUtils.getSubject().getPrincipal();

		if(param == null || isInvalidFileName(param.getFileName()) || !isOwnedDirectory(param.getParentId())){
			if (param != null && !isInvalidFileName(param.getFileName())) {
				return forbidden();
			}
			result.setCode(NetdiskErrMsgConstant.MKDIR_NAME_IS_NULL);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MKDIR_NAME_IS_NULL));
			return result;
		}
		param.setUserId(user.getId());
		param.setId(null);
		param.setOriginId(null);

		UserFile userFile = userFileMapper.findFileByRealName(param);
		if(userFile != null) {
			result.setCode(NetdiskErrMsgConstant.MKDIR_NAME_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MKDIR_NAME_EXIST));
			return result;
		}
		String fileKey = FileUtil.makeFileKey();
		param.setKey(fileKey);
		param.setIsDir(NetdiskConstant.FILE_TYPE_OF_DIR);
		param.setCreateTime(new Date());
		param.setUpdateTime(new Date());
		param.setStatus(NetdiskConstant.FILE_STATUS_OF_NORMAL);
		userFileMapper.insertSelective(param);
		return result;
	}

	@Transactional
	@Override
	public Result deleteFile(String fileKey) {

		Result result = new Result();
		if (StringUtils.isAnyEmpty(fileKey)) {
			result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.PARAM_IS_NULL));
			return result;
		}

		UserFile userFile = findOwnedFileByKey(fileKey);
		if (userFile == null) {
			return forbidden();
		}
		FileOrigin fileOrigin = fileOriginMapper.findByUserFileId(userFile.getId());
		User userThreadContext = (User) SecurityUtils.getSubject().getPrincipal();
		User user = userMapper.selectByPrimaryKeyForUpdate(userThreadContext.getId());

		long usedMemory = user.getUsedMemory();
		/** 	fileStatus
		 0 正常
		 1 主动删除
		 2 因删除文件夹被动删除*/

		if(userFile.getIsDir() == NetdiskConstant.FILE_IS_NOT_DIR){

			//如果是文件
			long fileSize = fileOrigin.getFileSize();
			usedMemory = usedMemory - fileSize;
			if (usedMemory < 0) {
				throw new RuntimeException("未知异常，请重试");
			}
			user.setUsedMemory(usedMemory);
		}else if (userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			//如果是文件夹，还要删除文件夹里面的子文件及子文件夹
			Map<String, Object> map = new HashMap<>();
			map.put("id", userFile.getId());
			map.put("sumsize", null);
			userFileMapper.getSumsizeDel(map);
			if(map.get("sumsize") != null) {
				long sumSize = ((Number) map.get("sumsize")).longValue();
				usedMemory = usedMemory - sumSize;
				if (usedMemory < 0) {
					throw new RuntimeException("未知异常，请重试");
				}
				user.setUsedMemory(usedMemory);
			}
			userFileMapper.deleteDir(userFile.getId());
		}
		Integer tag1 = userFileMapper.deleteFile(fileKey);
		Integer tag2 = userMapper.updateByPrimaryKeySelective(user);

		FileRecycle recycle = new FileRecycle();
		Date deleteTime = new Date();
		recycle.setDeleteTime(deleteTime);
		recycle.setDeleteUserId(user.getId());
		recycle.setFileId(userFile.getId());
		// long delayTime = 10*24*60*60*1000;
		// 设置超时时间，这个时间之类，文件还能从回收站恢复
		long delayTime = appConfiguration.getCustomConfiguration().getRecycleDelayTime();
		Date overTime = new Date(deleteTime.getTime() + delayTime);
		recycle.setOverTime(overTime);
		recycle.setRecycleStatus(NetdiskConstant.RECYCLE_STATUS_FILE_DEL);
		Integer tag3 = fileRecycleMapper.insertSelective(recycle);

		// userService.updatePrincipal();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					routeService.postMsgToRoute(recycle.getRecycleId().toString(), appConfiguration.getDelFilePath(), recycle, Result.class);
				} catch (RuntimeException e) {
					log.error("创建回收站清理任务失败: {}", recycle.getRecycleId(), e);
				}
			}
		});

		if(tag1 > 0 && tag2 > 0 && tag3 > 0){
			return result;
		}else {
			result.setCode(NetdiskErrMsgConstant.DELETE_FILE_FAILED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.DELETE_FILE_FAILED));
			return result;
		}
	}

	/**
	 * 移动文件
	 * @param param fileKey parentId
	 * @return
	 */
	@Transactional
	@Override
	public Result moveFile(UserFile param) {

		Result result = new Result();
		if (param == null || StringUtils.isEmpty(param.getKey()) || !isOwnedDirectory(param.getParentId())) {
			return forbidden();
		}
		UserFile userFile = findOwnedFileByKey(param.getKey());
		if (userFile == null) {
			return forbidden();
		}
		if (Objects.equals(userFile.getIsDir(), NetdiskConstant.FILE_IS_DIR)
				&& (Objects.equals(userFile.getId(), param.getParentId())
				|| userFileMapper.findChildIds(userFile.getId()).contains(param.getParentId()))) {
			result.setCode(NetdiskErrMsgConstant.MOVEFILE_FAILED);
			result.setMsg("不能将文件夹移动到自身或其子目录");
			return result;
		}
		if (userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			UserFile searchParam = new UserFile();
			searchParam.setParentId(param.getParentId());
			searchParam.setFileName(userFile.getFileName());
			searchParam.setUserId(userFile.getUserId());
			UserFile searchResult = userFileMapper.findFileByRealName(searchParam);
			if (searchResult != null) {
				// 移动失败，目标目录存在同名文件夹
				result.setCode(NetdiskErrMsgConstant.MOVEFILE_TARGET_DIR_EXIST_SAME_NAME_DIR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MOVEFILE_TARGET_DIR_EXIST_SAME_NAME_DIR));
				return result;
			}
		}

		userFile.setParentId(param.getParentId());
		userFileMapper.updateByPrimaryKeySelective(userFile);
		return result;
	}

	/**
	 * 查找文件路径（文件名）
	 * @param parentId
	 * @return
	 */
	@Override
	public Result findPathname(Integer parentId) {

		Result result = new Result();
		if (!isOwnedDirectory(parentId)) {
			return forbidden();
		}
		Map<String, Object> map = new HashMap<>();
		map.put("parentId", parentId);
		if (parentId == -1) {
			map.put("pathname", "/");
		}else {
			map.put("pathname", userFileMapper.findPathname(parentId));
		}
		result.setData(map);
		return result;
	}

	/**
	 * 文件下载
	 * @param fileKey
	 */
	@Override
	public void downLoad(String fileKey) {

		UserFile userFile = findOwnedFileByKey(fileKey);
		if (userFile == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		FileOrigin fileOrigin = fileOriginMapper.findByUserFileId(userFile.getId());
		// 得到文件的真实名字
		String fileRealName = userFile.getFileName();
		String filePath = fileOrigin.getFilePath();
		try (OutputStream out = response.getOutputStream()) {
			String fileName = URLEncoder.encode(fileRealName, "UTF-8");
			fileName = fileName.replace("+", "%20");
			// 设置响应头，控制浏览器下载该文件
			response.setHeader("content-disposition", "attachment;filename=" + fileName);
			// 设置文件大小
			response.addHeader("Content-Length", fileOrigin.getFileSize().toString());
			fastDFSClientWrapper.downloadFile(filePath, out);
		} catch (Exception e) {
			log.error("文件下载失败", e);
		}
	}

	/**
	 * 查找父文件下所有子文件
	 * @param fileId
	 * @return
	 */
	@Override
	public Result findChildIds(Integer fileId) {

		Result result = new Result();
		if (findOwnedFileById(fileId) == null) {
			return forbidden();
		}
		List<Integer> childIds = userFileMapper.findChildIds(fileId);
		result.setData(childIds);
		return result;
	}

	/**
	 * 前端页面搜索后，点击文件夹跳转，得到储存的文件路径
	 * @param fileId
	 * @return
	 */
	@Override
	public Result getPathStore(Integer fileId) {

		Result result = new Result();
		if (findOwnedFileById(fileId) == null) {
			return forbidden();
		}

		String path = userFileMapper.findIdPath(fileId);
		String[] ids = path.split("/");
		List<FilePathStore> paths = new ArrayList<>();
		for (String id : ids) {
			FilePathStore filePathStore = new FilePathStore();
			if ("-1".equals(id)) {
				filePathStore.setParentId(-1);
				filePathStore.setFileRealName("全部文件");
				paths.add(filePathStore);
				continue;
			}
			UserFile userFile = userFileMapper.selectByPrimaryKey(new Integer(id));

			filePathStore.setParentId(userFile.getId());
			filePathStore.setFileRealName(userFile.getFileName());
			paths.add(filePathStore);
		}
		result.setData(paths);
		return result;
	}

	/**
	 * findById
	 * @param id
	 * @return
	 */
	@Override
	public Result findById(Integer id) {

		Result result = new Result();
		if (id == null || id <= 0) {
			return Result.paramIsNull(result);
		}
		UserFile userFile = findOwnedFileById(id);
		if (userFile == null) {
			return forbidden();
		}
		result.setData(userFile);
		return result;
	}

	/**
	 * 获取文件媒体信息
	 * @param fileKey
	 * @return
	 */
	@Override
	public Result getFileMediaInfo(String fileKey) {

		Result result = new Result();
		if (findOwnedFileByKey(fileKey) == null) {
			return forbidden();
		}
		FileShowMedia fileShowMedia = fileMediaMapper.getFileMediaInfo(fileKey);
		result.setData(fileShowMedia);
		return result;
	}
}
