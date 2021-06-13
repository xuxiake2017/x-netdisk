package group.xuxiake.web.controller;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.UserFile;
import group.xuxiake.web.service.FileService;
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
	private FileService fileService;

	/**
	 * 文件上传
	 * @param parentId
	 * @param md5Hex
	 * @param lastModifiedDate
	 * @param fileRealName
	 * @param file
	 * @return
	 */
	@RequestMapping(value="/fileUpload")
	@ResponseBody
	public Result fileUplaod(Integer parentId, String md5Hex, Long lastModifiedDate, String fileRealName,  MultipartFile file) {
		return fileService.fileUpload(parentId, md5Hex, lastModifiedDate, fileRealName, file);
	}
	
	/*
	 * 删除文件
	 */
	@RequestMapping(value="/deleteFile")
	@ResponseBody
	public Result deleteFile(String fileKey) {
		return fileService.deleteFile(fileKey);
	}

	/*
	 * 下载文件
	 */
	@RequestMapping("/downLoad")
	public void downloadFile(String fileKey) {

		fileService.downLoad(fileKey);
	}

	/**
	 * 重命名文件
	 * @param userFile
	 * @return
	 */
	@RequestMapping(value="/reName")
	@ResponseBody
	public Result reName(UserFile userFile) {
		return fileService.reName(userFile);
	}

	/**
	 * 查找文件路径（文件名）
	 * @param parentId
	 * @return
	 */
	@RequestMapping("/findPathname")
	@ResponseBody
	public Result findPathname(Integer parentId){

		return fileService.findPathname(parentId);
	}

	/**
	 * 查找父文件下所有子文件
	 * @param fileId
	 * @return
	 */
	@RequestMapping("findChildIds")
	@ResponseBody
	public Result findChildIds(Integer fileId) {
		return fileService.findChildIds(fileId);
	}

	/**
	 * 前端页面搜索后，点击文件夹跳转，得到储存的文件路径
	 * @param fileId
	 * @return
	 */
	@RequestMapping("getPathStore")
	@ResponseBody
	public Result getPathStore(Integer fileId) {
		return fileService.getPathStore(fileId);
	}

	/**
	 * findById
	 * @param id
	 * @return
	 */
	@RequestMapping("findById")
	@ResponseBody
	public Result findById(Integer id) {
		return fileService.findById(id);
	}

	/**
	 * 获取文件媒体信息
	 * @param fileKey
	 * @return
	 */
	@RequestMapping("/getFileMediaInfo")
	@ResponseBody
	public Result getFileMediaInfo(String fileKey) {
		return fileService.getFileMediaInfo(fileKey);
	}

}
