
package group.xuxiake.web.controller;

import group.xuxiake.common.entity.Page;
import group.xuxiake.web.service.ImageService;
import group.xuxiake.common.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 用于处理图片的Controller
 * @author xuxiake
 *
 */
@Controller
@RequestMapping("/img")
public class ImageController {

	@Resource
	private ImageService imageService;
	/*
	 * 得到图片列表
	 */
	@RequestMapping("/toImgList")
	@ResponseBody
	public Result toImgList(Page page) {
		return imageService.toImgList(page);
	}

	@RequestMapping("/getGalleryList")
	@ResponseBody
	public Result getGalleryList() {
		return imageService.getGalleryList();
	}

	@RequestMapping("/getGalleryNum")
	@ResponseBody
	public Result getGalleryNum() {
		return imageService.getGalleryNum();
	}
}
