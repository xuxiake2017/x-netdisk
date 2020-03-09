package group.xuxiake.web.service.impl;

import com.drew.imaging.ImageProcessingException;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.configuration.CustomConfiguration;
import group.xuxiake.common.entity.FileUpload;
import group.xuxiake.common.entity.Recycle;
import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.entity.show.FilePathStore;
import group.xuxiake.web.handler.LowKbpsHandler;
import group.xuxiake.web.handler.RecycleHandler;
import group.xuxiake.web.handler.VideoTransformHandler;
import group.xuxiake.common.mapper.FileUploadMapper;
import group.xuxiake.common.mapper.RecycleMapper;
import group.xuxiake.common.mapper.UserNetdiskMapper;
import group.xuxiake.web.service.FileUploadService;
import group.xuxiake.web.service.UserNetdiskService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@Service("fileUploadService")
public class FileUploadServiceImpl implements FileUploadService {

	@Resource
	private FileUploadMapper fileUploadMapper;
	@Resource
	private UserNetdiskMapper userNetdiskMapper;
	@Resource
	private RecycleMapper recycleMapper;
	@Resource
	private UserNetdiskService userNetdiskService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Resource
    private FastDFSClientWrapper fastDFSClientWrapper;
	@Resource
    private AppConfiguration appConfiguration;

	/**
	 * 检查文件MD5值是否在数据库存在
	 * @param md5Hex
	 * @return
	 */
	@Override
	public Result checkMd5(String md5Hex) {
		Result result = new Result();
		// 根据前端传来的文件MD5值去数据库查找
		List<FileUpload> files = fileUploadMapper.findFileByMd5Hex(md5Hex);
		if (files.size() == 0) {
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
	 * @param fileUpload
	 * @return
	 */
	@Override
	public Result uploadMD5(FileUpload fileUpload) {

		Result result = new Result();
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();

        CustomConfiguration customConfiguration = appConfiguration.getCustomConfiguration();

        // 检查文件大小
		try {
			// 普通用户
			if (Integer.valueOf(userNetdisk.getUserStatus()) == NetdiskConstant.USER_STATUS_NORMAL) {
				if (Long.valueOf(fileUpload.getFileSize()) > customConfiguration.getFileSizeMax()) {
					result.setCode(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT);
					result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT));
					return result;
				}
			}
			// vip用户
			if (Integer.valueOf(userNetdisk.getUserStatus()) == NetdiskConstant.USER_STATUS_VIP) {
				if (Long.valueOf(fileUpload.getFileSize()) > customConfiguration.getVipFileSizeMax()) {
					result.setCode(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT);
					result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT));
					return result;
				}
			}

			// 查询剩余空间
			long totalMemory = Long.valueOf(userNetdisk.getTotalMemory());
			long usedMemory = Long.valueOf(userNetdisk.getUsedMemory());
			long availableMemory = totalMemory - usedMemory;
			if (availableMemory < Long.valueOf(fileUpload.getFileSize())) {
				result.setCode(NetdiskErrMsgConstant.CHECKMD5_AVAIABLE_MEMORY_NOT_ENOUGH);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.CHECKMD5_AVAIABLE_MEMORY_NOT_ENOUGH));
				return result;
			}

			// 根据前端传来的文件MD5值去数据库查找
			FileUpload fileUploadByMd5 = fileUploadMapper.findFileByMd5Hex(fileUpload.getMd5Hex()).get(0);

			usedMemory = usedMemory + Long.valueOf(fileUpload.getFileSize());
			userNetdisk.setUsedMemory(usedMemory + "");
			userNetdiskMapper.updateByPrimaryKey(userNetdisk);

			String fileExtName = fileUpload.getFileRealName()
					.substring(fileUpload.getFileRealName().lastIndexOf(".") + 1);
			String fileSaveName = FileUtil.makeFileSaveName(fileExtName);
			Date uploadTime = new Date();
			Integer fileType = FileUtil.getFileType(fileExtName);

			fileUpload.setUploadUserId(userNetdisk.getId() + "");
			fileUpload.setFileType(fileType);
			fileUpload.setFileExtName(fileExtName);
			fileUpload.setFileSaveName(fileSaveName);
			fileUpload.setUploadTime(uploadTime);
			fileUpload.setImgWidth(fileUploadByMd5.getImgWidth());
			fileUpload.setImgHeight(fileUploadByMd5.getImgHeight());
			fileUpload.setShootTime(fileUploadByMd5.getShootTime());
			fileUpload.setFilePath(fileUploadByMd5.getFilePath());
			fileUpload.setMediaCachePath(fileUploadByMd5.getMediaCachePath());
			fileUploadMapper.insertSelective(fileUpload);

			userNetdiskService.updatePrincipal();
			return result;
		} catch (Exception e) {
			log.error("检查md5失败", e);
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
		FileUpload fileUpload = new FileUpload();
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();

		fileUpload.setUploadUserId(userNetdisk.getId()+"");
		fileUpload.setParentId(parentId);
		List<FileUpload> list = fileUploadMapper.findAllDir(fileUpload);
		Result result = new Result();
		result.setData(list);
		return result;
	}

	/**
	 * 文件重命名
	 * @param fileUpload
	 * @return
	 */
	@Override
	public Result reName(FileUpload fileUpload) {
		Result result = new Result();
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
		fileUpload.setUploadUserId(userNetdisk.getId()+"");

		if (fileUpload.getIsDir()==0) {
			if (fileUpload.getFileRealName().equals(
					fileUploadMapper.findFileBySaveName(
							fileUpload.getFileSaveName()).getFileRealName())) {
				return result;
			}
			//是文件夹，要判断当前目录下是否有同名文件夹
			List<FileUpload> list = fileUploadMapper.findFileByRealName(fileUpload);
			if (list.size()>0) {
				//该目录下有同名文件夹
				result.setCode(NetdiskErrMsgConstant.RENAME_FILE_NAME_EXIST);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.RENAME_FILE_NAME_EXIST));
				return result;
			}
		}
		Integer tag = fileUploadMapper.updateFileSelective(fileUpload);
		if (tag>0) {
			return result;
		}else {
			result.setCode(NetdiskErrMsgConstant.RENAME_FILE_FAILED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.RENAME_FILE_FAILED));
			return result;
		}
	}

	/**
	 * 文件上传
	 * @param parentId
	 * @param md5Hex
	 * @param lastModifiedDate
	 * @param file
	 * @return
	 */
	@Override
	public Result fileUpload(Integer parentId, String md5Hex, Long lastModifiedDate, MultipartFile file) {
		// 消息提示
		Result result = new Result();
		ByteArrayOutputStream baos = null;
		BufferedInputStream bis = null;
        String fdfsNginxServer = appConfiguration.getFdfsNginxServer();
        try {

			UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();

			if (file.isEmpty()) {
				result.setCode(NetdiskErrMsgConstant.UPLOAD_FILE_IS_NULL);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UPLOAD_FILE_IS_NULL));
				return result;
			}
			// 得到上传的文件名称，
			String fileRealName = file.getOriginalFilename();
			// 得到上传文件的扩展名
			String fileExtName = "";
			if (fileRealName.contains(".")) {
				fileExtName = fileRealName.substring(fileRealName.lastIndexOf(".") + 1);
			}
			//文件大小
			long fileSize = file.getSize();
			// 查询剩余空间
			long totalMemory = Long.valueOf(userNetdisk.getTotalMemory());
			long usedMemory = Long.valueOf(userNetdisk.getUsedMemory());
			long availableMemory = totalMemory - usedMemory;
			if (availableMemory < fileSize) {
				result.setCode(NetdiskErrMsgConstant.UPLOAD_AVAIABLE_MEMORY_NOT_ENOUGH);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UPLOAD_AVAIABLE_MEMORY_NOT_ENOUGH));
				return result;
			}
			//根据文件后缀判断文件类型
			Integer fileType = FileUtil.getFileType(fileExtName);
			// 获取上传文件的输入流
			InputStream in = file.getInputStream();
			FileUpload fileUpload = new FileUpload();
			if (parentId == null) {
				parentId = -1;
			}
			fileUpload.setUploadUserId(userNetdisk.getId() + "");
			fileUpload.setParentId(parentId);
			fileUpload.setFileExtName(fileExtName);
			fileUpload.setFileRealName(fileRealName);
			fileUpload.setFileSize(fileSize + "");
			fileUpload.setFileType(fileType);
			fileUpload.setMd5Hex(md5Hex);

            String filePath = fastDFSClientWrapper.uploadFile(file);
            String fileSaveName = FileUtil.makeFileSaveName(fileExtName);

			// 获得上传时间
			Date uploadTime = new Date();
			fileUpload.setUploadTime(uploadTime);

			//获得图片的信息（拍摄时间、宽、高）
			if (fileType==2) {
				try {
					InputStream is = fastDFSClientWrapper.getInputStream(filePath);
					Map<String, Object> imgInfo = ImageUtil.getImgInfo(is, new Long(lastModifiedDate));
					fileUpload.setShootTime((Date) imgInfo.get("shootTime"));
					fileUpload.setImgHeight((Integer) imgInfo.get("imgHeight"));
					fileUpload.setImgWidth((Integer) imgInfo.get("imgWidth"));
				} catch (ImageProcessingException e) {
					fileUpload.setShootTime(new Date(new Long(lastModifiedDate)));
					fileUpload.setImgHeight(1000);
					fileUpload.setImgWidth(1000);
				}
				String mediaCachePath = null;
				//如果图片大小超过100kb，对图片进行缩略，否则不缩略
				if (fileSize>=100*1024) {
					InputStream is = fastDFSClientWrapper.getInputStream(filePath);
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					Thumbnails.of(is).size(500, 500).toOutputStream(byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    mediaCachePath = fastDFSClientWrapper.uploadFile(inputStream, bytes.length, fileExtName);
				}else {
					mediaCachePath = filePath;
				}
				mediaCachePath = fdfsNginxServer + "/" + mediaCachePath;
				fileUpload.setMediaCachePath(mediaCachePath);
			}
			fileUpload.setFilePath(filePath);
			fileUpload.setFileSaveName(fileSaveName);
			// 将上传文件的相关信息保存到数据库
			fileUploadMapper.insertSelective(fileUpload);

			usedMemory = usedMemory + fileSize;
			userNetdisk.setUsedMemory(usedMemory + "");
			userNetdiskMapper.updateByPrimaryKeySelective(userNetdisk);
			// 对于上传上来的视频，开辟线程进行转码，方便在线播放
			if (fileType == 4) {
				String cacheParentPath = request.getServletContext().getRealPath("cache");
				cacheParentPath = cacheParentPath.replaceAll("\\\\", "/");

				String cachePathBefore = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileSaveName(fileUpload.getFileExtName()));
				String cachePathMiddle = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileSaveName("mp4"));
				String cachePathAfter = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileSaveName("mp4"));

				InputStream is = fastDFSClientWrapper.getInputStream(filePath);
				FileOutputStream fos = new FileOutputStream(new File(cachePathBefore));
				IOUtils.copy(is, fos);
				is.close();
				fos.close();
				String[] paths = {cachePathBefore, cachePathMiddle, cachePathAfter};
				//System.out.println("[视频开始转码]");
				new Thread(new VideoTransformHandler(fileUploadMapper, fastDFSClientWrapper, paths, fileUpload, fdfsNginxServer)).start();
			}
			if ("mp3".equals(fileExtName)) {
				// lowKmps，降低mp3码率
				String cacheParentPath = request.getServletContext().getRealPath("cache");
				cacheParentPath = cacheParentPath.replaceAll("\\\\", "/");

				String cachePathBefore = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileSaveName(fileUpload.getFileExtName()));
				String cachePathAfter = FileUtil.makeCachePath(cacheParentPath, FileUtil.makeFileSaveName(fileUpload.getFileExtName()));

				InputStream is = fastDFSClientWrapper.getInputStream(filePath);
				FileOutputStream fos = new FileOutputStream(new File(cachePathBefore));
				IOUtils.copy(is, fos);
				is.close();
				fos.close();
				String[] paths = {cachePathBefore,cachePathAfter};

				//System.out.println("[音频开始降低码率]");
				new Thread(new LowKbpsHandler(fileUploadMapper, fastDFSClientWrapper, paths, fileUpload, fdfsNginxServer)).start();
			}

		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			result.setCode(NetdiskErrMsgConstant.UPLOAD_TIMEOUT);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UPLOAD_TIMEOUT));
			return result;
		} catch (Exception e) {
			log.error("上传文件失败", e);
			result.setCode(NetdiskErrMsgConstant.UPLOAD_FAILED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.UPLOAD_FAILED));
			return result;
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		userNetdiskService.updatePrincipal();

		return result;
	}

	/**
	 * 创建文件夹
	 * @param fileUpload
	 * @return
	 */
	@Override
	public Result mkDir(FileUpload fileUpload) {

		Result result = new Result();
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();

		if(fileUpload.getFileRealName() == "" || fileUpload.getFileRealName() == null){
			result.setCode(NetdiskErrMsgConstant.MKDIR_NAME_IS_NULL);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MKDIR_NAME_IS_NULL));
			return result;
		}
		fileUpload.setUploadUserId(userNetdisk.getId() + "");

		List<FileUpload> fileList = fileUploadMapper.findFileByRealName(fileUpload);
		if(fileList.size() >= 1){
			result.setCode(NetdiskErrMsgConstant.MKDIR_NAME_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MKDIR_NAME_EXIST));
			return result;
		}
		String fileSaveName = FileUtil.makeDirSaveName();
		fileUpload.setFileSaveName(fileSaveName);
		fileUpload.setUploadTime(new Date());
		fileUpload.setFileType(NetdiskConstant.FILE_TYPE_OF_DIR);
		fileUpload.setIsDir(NetdiskConstant.FILE_IS_DIR);
		if(fileUploadMapper.insertSelective(fileUpload) > 0){
			return result;
		}else {
			result.setCode(NetdiskErrMsgConstant.MKDIR_FAILED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MKDIR_FAILED));
		}
		return result;
	}

	@Override
	public Result deleteFile(String fileSaveName) {

		Result result = new Result();
		if (fileSaveName == null || "".equals(fileSaveName)) {
			result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.PARAM_IS_NULL));
			return result;
		}

		FileUpload fileUpload = fileUploadMapper.findFileBySaveName(fileSaveName);
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();

		long usedMemory = Long.valueOf(userNetdisk.getUsedMemory());
		/** 	fileStatus
		 0 正常
		 1 主动删除
		 2 因删除文件夹被动删除*/

		if(fileUpload.getIsDir() == NetdiskConstant.FILE_IS_NOT_DIR){

			//如果是文件
			long fileSize = Long.valueOf(fileUpload.getFileSize());
			usedMemory = usedMemory - fileSize;
			userNetdisk.setUsedMemory(usedMemory + "");
		}else if (fileUpload.getIsDir() == NetdiskConstant.FILE_IS_DIR) {
			//如果是文件夹，还要删除文件夹里面的子文件及子文件夹
			Map<String, Object> map = new HashMap<>();
			map.put("id", fileUpload.getId());
			map.put("sumsize", null);
			fileUploadMapper.getSumsizeDel(map);
			if(map.get("sumsize") != null) {
				Long sumSize = Long.valueOf(map.get("sumsize") + "");
				usedMemory = usedMemory - sumSize;
				userNetdisk.setUsedMemory(usedMemory+"");
			}
			fileUploadMapper.deleteDir(fileUpload.getId());
		}
		Integer tag1 = fileUploadMapper.deleteFile(fileSaveName);
		Integer tag2 = userNetdiskMapper.updateByPrimaryKeySelective(userNetdisk);

		Recycle recycle = new Recycle();
		Date deleteTime = new Date();
		recycle.setDeleteTime(deleteTime);
		recycle.setDeleteUserId(userNetdisk.getId());
		recycle.setFileId(fileUpload.getId());
		//long delayTime = 10*24*60*60*1000;
		//设置超时时间，这个时间之类，文件还能从回收站恢复
		long delayTime = appConfiguration.getCustomConfiguration().getRecycleDelayTime();
		Date overTime = new Date(deleteTime.getTime() + delayTime);
		recycle.setOverTime(overTime);
		recycle.setRecycleStatus("0");
		Integer tag3 = recycleMapper.insertSelective(recycle);

		userNetdiskService.updatePrincipal();

		// 添加至内存库
		RecycleHandler.put(recycle);

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
	 * @param fileUpload
	 * @return
	 */
	@Override
	public Result moveFile(FileUpload fileUpload) {

		Result result = new Result();
		FileUpload fileUploadSrc = fileUploadMapper.findFileBySaveName(fileUpload.getFileSaveName());
		FileUpload fileUploadSearch = new FileUpload();
		fileUploadSearch.setParentId(fileUpload.getParentId());
		fileUploadSearch.setFileRealName(fileUploadSrc.getFileRealName());
		fileUploadSearch.setUploadUserId(fileUploadSrc.getUploadUserId());
		List<FileUpload> list = fileUploadMapper.findFileByRealName(fileUploadSearch);
		if (list != null && list.size() > 0) {
			// 移动失败，目标目录存在同名文件夹
			result.setCode(NetdiskErrMsgConstant.MOVEFILE_TARGET_DIR_EXIST_SAME_NAME_DIR);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MOVEFILE_TARGET_DIR_EXIST_SAME_NAME_DIR));
			return result;
		}

		fileUpload.setUploadUserId(fileUploadSrc.getUploadUserId());
		if(fileUploadMapper.moveFile(fileUpload) > 0) {
			return result;
		}else {
			result.setCode(NetdiskErrMsgConstant.MOVEFILE_FAILED);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.MOVEFILE_FAILED));
			return result;
		}
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
			map.put("pathname", fileUploadMapper.findPathname(parentId));
		}
		result.setData(map);
		return result;
	}

	/**
	 * 文件下载
	 * @param fileUpload
	 */
	@Override
	public void downLoad(FileUpload fileUpload) {

		FileUpload fileUploadNew = fileUploadMapper.findFileBySaveName(fileUpload.getFileSaveName());
		// 得到文件的真实名字
		String fileRealName = fileUploadNew.getFileRealName();
		String filePath = fileUploadNew.getFilePath();
		OutputStream out = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(fastDFSClientWrapper.getInputStream(filePath));
			String fileName = URLEncoder.encode(fileRealName, "UTF-8");
			fileName = fileName.replace("+", "%20");
			// 设置响应头，控制浏览器下载该文件
			response.setHeader("content-disposition", "attachment;filename=" + fileName);
			// 设置文件大小
			response.addHeader("Content-Length", fileUploadNew.getFileSize());
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
	 * 查找父文件下所有子文件
	 * @param fileId
	 * @return
	 */
	@Override
	public Result findChildIds(Integer fileId) {

		Result result = new Result();
		List<Integer> childIds = fileUploadMapper.findChildIds(fileId);
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

		String path = fileUploadMapper.findIdPath(fileId);
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
			FileUpload file = fileUploadMapper.selectByPrimaryKey(new Integer(id));

			filePathStore.setParentId(file.getId());
			filePathStore.setFileRealName(file.getFileRealName());
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
		FileUpload fileUpload = fileUploadMapper.selectByPrimaryKey(id);
		result.setData(fileUpload);
		return result;
	}
}
