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
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.MultimediaInfo;
import it.sauronsoftware.jave.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
		User user = (User) SecurityUtils.getSubject().getPrincipal();

        CustomConfiguration customConfiguration = appConfiguration.getCustomConfiguration();

        // 检查文件大小
		try {
			// 普通用户
			if (user.getUserStatus() == NetdiskConstant.USER_STATUS_NORMAL) {
				if (param.getFileSize() > customConfiguration.getFileSizeMax()) {
					result.setCode(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT);
					result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT));
					return result;
				}
			}
			// vip用户
			if (user.getUserStatus() == NetdiskConstant.USER_STATUS_VIP) {
				if (param.getFileSize() > customConfiguration.getVipFileSizeMax()) {
					result.setCode(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT);
					result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT));
					return result;
				}
			}

			// 查询剩余空间
			long totalMemory = user.getTotalMemory();
			long usedMemory = user.getUsedMemory();
			long availableMemory = totalMemory - usedMemory;
			if (availableMemory < param.getFileSize()) {
				result.setCode(NetdiskErrMsgConstant.CHECKMD5_AVAIABLE_MEMORY_NOT_ENOUGH);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_AVAIABLE_MEMORY_NOT_ENOUGH));
				return result;
			}

			// 根据前端传来的文件MD5值去数据库查找
			FileOrigin fileOrigin = fileOriginMapper.findFileByMd5Hex(param.getMd5Hex());

			usedMemory = usedMemory + param.getFileSize();
			user.setUsedMemory(usedMemory);
			userMapper.updateByPrimaryKey(user);

			String fileKey = FileUtil.makeFileKey();
			UserFile userFile = new UserFile();
			userFile.setOriginId(fileOrigin.getId());
			userFile.setFileName(param.getFileRealName());
			if (param.getParentId() == -1) {
				userFile.setFilePath("/");
			} else {
				userFile.setFilePath(userFileMapper.findPathname(param.getParentId()));
			}
			userFile.setIsDir(NetdiskConstant.FILE_IS_NOT_DIR);
			userFile.setUserId(user.getId());
			userFile.setParentId(param.getParentId());
			userFile.setKey(fileKey);
			userFile.setStatus(NetdiskConstant.FILE_STATUS_OF_NORMAL);
			userFile.setCreateTime(new Date());
			userFile.setUpdateTime(new Date());
			userFileMapper.insertSelective(userFile);

			userService.updatePrincipal();
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
		User user = (User) SecurityUtils.getSubject().getPrincipal();

		if (param.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			if (param.getFileName().equals(
					userFileMapper.findFileByKey(
							param.getKey()).getFileName())) {
				return result;
			}
			//是文件夹，要判断当前目录下是否有同名文件夹
			UserFile userFile = userFileMapper.findFileByRealName(param);
			if (userFile != null) {
				//该目录下有同名文件夹
				result.setCode(NetdiskErrMsgConstant.RENAME_FILE_NAME_EXIST);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.RENAME_FILE_NAME_EXIST));
				return result;
			}
		}
		userFileMapper.updateByKeySelective(param);
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

			User user = (User) SecurityUtils.getSubject().getPrincipal();

			if (file.isEmpty()) {
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
			if (parentId == -1) {
				userFile.setFilePath("/");
			} else {
				userFile.setFilePath(userFileMapper.findPathname(parentId));
			}
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
		userService.updatePrincipal();

		return result;
	}

	/**
	 * 处理媒体文件、转码
	 * @param fileOrigin
	 * @throws IOException
	 */
	public void handleMediaFile(FileOrigin fileOrigin, Long lastModifiedDate) throws IOException, EncoderException {

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

			byte[] fileBytes = fastDFSClientWrapper.getFileBytes(filePath);
			if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_TXT) {
				fileBytes = FileUtil.textEncodingConvert(fileBytes);
			}
			byte[] bytes = null;
			try (InputStream in = new ByteArrayInputStream(fileBytes)) {
				bytes = docConverterUtil.docToPDF(in, fileOrigin.getFileExtName());
			}
			try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);) {
				String previewUrl = fastDFSClientWrapper.uploadFile(inputStream, bytes.length, "pdf");
				previewUrl = fdfsNginxServer + "/" + previewUrl;
				fileOrigin.setPreviewUrl(previewUrl);
				fileOriginMapper.updateByPrimaryKeySelective(fileOrigin);
			}
		}

		if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_PDF) {

			String previewUrl = fdfsNginxServer + "/" + fileOrigin.getFilePath();
			fileOrigin.setPreviewUrl(previewUrl);
			fileOriginMapper.updateByPrimaryKeySelective(fileOrigin);
		}

		//获得图片的信息（拍摄时间、宽、高）
		if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_PIC) {
			try (InputStream is = fastDFSClientWrapper.getInputStream(filePath)) {
				Map<String, Object> imgInfo = ImageUtil.getImgInfo(is, lastModifiedDate);
				fileMedia.setShootTime((Date) imgInfo.get("shootTime"));
				fileMedia.setImgHeight((Integer) imgInfo.get("imgHeight"));
				fileMedia.setImgWidth((Integer) imgInfo.get("imgWidth"));
			} catch (ImageProcessingException e) {
				fileMedia.setShootTime(new Date(lastModifiedDate));
				fileMedia.setImgHeight(1000);
				fileMedia.setImgWidth(1000);
			}
			String previewUrl = filePath;
			ByteArrayOutputStream byteArrayOutputStream = null;
			ByteArrayInputStream inputStream = null;
			try (InputStream is = fastDFSClientWrapper.getInputStream(filePath)) {
				//如果图片大小超过100kb，对图片进行缩略，否则不缩略
				if (fileOrigin.getFileSize() >= 100 * 1024) {
					byteArrayOutputStream = new ByteArrayOutputStream();
					Thumbnails.of(is).size(1000, 1000).toOutputStream(byteArrayOutputStream);
					byte[] bytes = byteArrayOutputStream.toByteArray();
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
			String[] paths = {cachePathBefore, cachePathMiddle, cachePathAfter};

			try (InputStream is = fastDFSClientWrapper.getInputStream(filePath);
				 FileOutputStream fos = new FileOutputStream(new File(cachePathBefore))) {
				IOUtils.copy(is, fos);
			}
			// 获取视频分辨率时长信息
			MultimediaInfo mi = new Encoder().getInfo(new File(paths[0]));
			Long duration = mi.getDuration();
			VideoInfo videoInfo = mi.getVideo();
			int height = videoInfo.getSize().getHeight();
			int width = videoInfo.getSize().getWidth();
			fileMedia.setVideoDuration(duration.intValue());
			fileMedia.setVideoHeight(height);
			fileMedia.setVideoWidth(width);

			// 获取缩率图
			String thumbnailCachePath = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName("jpg"));
			ConvertVideoUtil.thumbnail(cachePathBefore, thumbnailCachePath, duration);
			File thumbnailCacheFile = new File(thumbnailCachePath);
			String thumbnailPath = fastDFSClientWrapper.uploadFile(thumbnailCacheFile, "jpg");
			fileMedia.setThumbnailUrl(fdfsNginxServer + "/" + thumbnailPath);
			thumbnailCacheFile.delete();

			new Thread(new VideoTransformHandler(fileOriginMapper, fastDFSClientWrapper, paths, fileOrigin, fdfsNginxServer, fileMedia)).start();
		}
		if (fileOrigin.getFileType() == NetdiskConstant.FILE_TYPE_OF_MUSIC) {
			// lowKmps，降低mp3码率

			String cachePathBefore = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName(fileOrigin.getFileExtName()));
			String cachePathAfter = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileTempName(fileOrigin.getFileExtName()));

			try (InputStream is = fastDFSClientWrapper.getInputStream(filePath);
				 FileOutputStream fos = new FileOutputStream(new File(cachePathBefore))) {
				IOUtils.copy(is, fos);
			}
			String[] paths = {cachePathBefore,cachePathAfter};

			ByteArrayOutputStream byteArrayOutputStream = null;
			ByteArrayInputStream byteArrayInputStream = null;
			String musicPoster = "";
			try {
				MP3Info mp3Info = MP3Utils.getMP3Info(paths[0]);
				fileMedia.setMusicArtist(mp3Info.getArtist());
				if (mp3Info.getAlbumImage() != null) {
					if (mp3Info.getAlbumImage().length > 1024 * 100) { // 封面大于100KB要进行缩略
						byteArrayInputStream = new ByteArrayInputStream(mp3Info.getAlbumImage());
						byteArrayOutputStream = new ByteArrayOutputStream();
						Thumbnails.of(byteArrayInputStream).size(1000, 1000).toOutputStream(byteArrayOutputStream);
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
			new Thread(new LowKbpsHandler(fileOriginMapper, fastDFSClientWrapper, paths, fileOrigin, fdfsNginxServer)).start();
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

		if(StringUtils.isAnyEmpty(param.getFileName())){
			result.setCode(NetdiskErrMsgConstant.MKDIR_NAME_IS_NULL);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MKDIR_NAME_IS_NULL));
			return result;
		}
		param.setUserId(user.getId());

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
		if (param.getParentId() == -1) {
			param.setFilePath("/");
		} else {
			param.setFilePath(userFileMapper.findPathname(param.getParentId()));
		}
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

		UserFile userFile = userFileMapper.findFileByKey(fileKey);
		FileOrigin fileOrigin = fileOriginMapper.findByUserFileId(userFile.getId());
		User user = (User) SecurityUtils.getSubject().getPrincipal();

		long usedMemory = user.getUsedMemory();
		/** 	fileStatus
		 0 正常
		 1 主动删除
		 2 因删除文件夹被动删除*/

		if(userFile.getIsDir() == NetdiskConstant.FILE_IS_NOT_DIR){

			//如果是文件
			long fileSize = fileOrigin.getFileSize();
			usedMemory = usedMemory - fileSize;
			user.setUsedMemory(usedMemory);
		}else if (userFile.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			//如果是文件夹，还要删除文件夹里面的子文件及子文件夹
			Map<String, Object> map = new HashMap<>();
			map.put("id", userFile.getId());
			map.put("sumsize", null);
			userFileMapper.getSumsizeDel(map);
			if(map.get("sumsize") != null) {
				Long sumSize = new Long((Integer) map.get("sumsize"));
				usedMemory = usedMemory - sumSize;
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

		userService.updatePrincipal();

		routeService.postMsgToRoute(recycle.getRecycleId().toString(), appConfiguration.getDelFilePath(), recycle, Result.class);

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
		UserFile userFile = userFileMapper.findFileByKey(param.getKey());
		String targetPatentFilePath = "/";
		if (param.getParentId() != -1) {
            targetPatentFilePath = userFileMapper.findPathname(param.getParentId());
        }
		String nowParentFilePath = "/";
		if (userFile.getParentId() != -1) {
			nowParentFilePath = userFileMapper.findPathname(userFile.getParentId());
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

		// 更新filePath
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

		userFile.setParentId(param.getParentId());
        userFile.setFilePath(null);
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

		UserFile userFile = userFileMapper.findFileByKey(fileKey);
		FileOrigin fileOrigin = fileOriginMapper.findByUserFileId(userFile.getId());
		// 得到文件的真实名字
		String fileRealName = userFile.getFileName();
		String filePath = fileOrigin.getFilePath();
		OutputStream out = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(fastDFSClientWrapper.getInputStream(filePath));
			String fileName = URLEncoder.encode(fileRealName, "UTF-8");
			fileName = fileName.replace("+", "%20");
			// 设置响应头，控制浏览器下载该文件
			response.setHeader("content-disposition", "attachment;filename=" + fileName);
			// 设置文件大小
			response.addHeader("Content-Length", fileOrigin.getFileSize().toString());
			out = response.getOutputStream();
			byte buffer[] = new byte[1024];
			int len = -1;
			while ((len = bis.read(buffer)) != -1) {
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
				log.error(e.getMessage(), e);
			}
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
		UserFile userFile = userFileMapper.selectByPrimaryKey(id);
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
		FileShowMedia fileShowMedia = fileMediaMapper.getFileMediaInfo(fileKey);
		result.setData(fileShowMedia);
		return result;
	}
}
