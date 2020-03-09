package group.xuxiake.web.controller;

import group.xuxiake.common.entity.FileUpload;
import group.xuxiake.web.service.FileUploadService;
import group.xuxiake.common.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 检查md5值的Controller
 * 
 * @author xuxiake
 *
 */
@Controller
@RequestMapping("/file")
public class CheckMd5Controller {

	@Resource
	private FileUploadService fileUploadService;

	/*
	 * 检查文件的md5值
	 */
	@RequestMapping(value = "/checkMd5")
	@ResponseBody
	public Result checkMd5(String md5Hex) {
		
		return fileUploadService.checkMd5(md5Hex);
	}

	/**
	 * MD5值已存在（假上传）
	 * @param fileUpload
	 * @return
	 */
	@RequestMapping("/uploadMD5")
	@ResponseBody
	public Result uploadMD5(FileUpload fileUpload) {
		return fileUploadService.uploadMD5(fileUpload);
	}
}
