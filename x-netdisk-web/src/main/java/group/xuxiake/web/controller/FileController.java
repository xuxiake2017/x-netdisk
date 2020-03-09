package group.xuxiake.web.controller;

import group.xuxiake.common.entity.FileUpload;
import group.xuxiake.common.entity.Result;
import group.xuxiake.web.service.FileUploadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 用于文件操作的Controller
 * @author xuxiake
 *
 */
@Controller
@RequestMapping("/file")
public class FileController {

	@Resource
	private FileUploadService fileUploadService;

	/**
	 * 文件上传
	 * @param parentId
	 * @param md5Hex
	 * @param lastModifiedDate
	 * @param file
	 * @return
	 */
	@RequestMapping(value="/fileUpload")
	@ResponseBody
	public Result fileUplaod(Integer parentId, String md5Hex, Long lastModifiedDate, MultipartFile file) {
		return fileUploadService.fileUpload(parentId, md5Hex, lastModifiedDate, file);
	}
	
	/*
	 * 删除文件
	 */
	@RequestMapping(value="/deleteFile")
	@ResponseBody
	public Result deleteFile(String fileSaveName) {
		return fileUploadService.deleteFile(fileSaveName);
	}

	/*
	 * 下载文件
	 */
	@RequestMapping("/downLoad")
	public void downloadFile(FileUpload fileUpload) {

		fileUploadService.downLoad(fileUpload);
	}

	/**
	 * 重命名文件
	 * @param fileUpload
	 * @return
	 */
	@RequestMapping(value="/reName")
	@ResponseBody
	public Result reName(FileUpload fileUpload) {
		return fileUploadService.reName(fileUpload);
	}

	/**
	 * 查找文件路径（文件名）
	 * @param parentId
	 * @return
	 */
	@RequestMapping("/findPathname")
	@ResponseBody
	public Result findPathname(Integer parentId){

		return fileUploadService.findPathname(parentId);
	}

	/**
	 * 查找父文件下所有子文件
	 * @param fileId
	 * @return
	 */
	@RequestMapping("findChildIds")
	@ResponseBody
	public Result findChildIds(Integer fileId) {
		return fileUploadService.findChildIds(fileId);
	}

	/**
	 * 前端页面搜索后，点击文件夹跳转，得到储存的文件路径
	 * @param fileId
	 * @return
	 */
	@RequestMapping("getPathStore")
	@ResponseBody
	public Result getPathStore(Integer fileId) {
		return fileUploadService.getPathStore(fileId);
	}

	/**
	 * findById
	 * @param id
	 * @return
	 */
	@RequestMapping("findById")
	@ResponseBody
	public Result findById(Integer id) {
		return fileUploadService.findById(id);
	}

}
