package group.xuxiake.web.controller;

import group.xuxiake.common.entity.Page;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.param.ShareFileParam;
import group.xuxiake.web.service.ShareFileService;
import group.xuxiake.web.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 分享文件的Controller
 * @author xuxiake
 *
 */
@Controller
@RequestMapping("/share")
public class ShareFileController {

	@Resource
	private ShareFileService shareFileService;

	/**
	 * 获取分享文件
	 * @param shareId
	 * @return
	 */
	@RequestMapping(value = "/getShareFile", method = RequestMethod.GET)
	@ResponseBody
	public Result getShareFile(String shareId) {
		
		return shareFileService.getShareFile(shareId);
	}

	/**
	 * 检查提取密码
	 * @param shareId 分享文件shareId
	 * @param sharePwd 分享文件密码
	 * @return
	 */
	@RequestMapping(value="/checkPwd", method = RequestMethod.POST)
	@ResponseBody
	public Result checkPwd(String shareId, String sharePwd) {

		return shareFileService.checkPwd(shareId, sharePwd);
	}

	/**
	 * 查询文件夹子目录
	 * @param shareId
	 * @param sharePwd
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value="/getSubList", method = RequestMethod.POST)
	@ResponseBody
	public Result getSubList(String shareId, String sharePwd, Integer parentId) {

		return shareFileService.getSubList(shareId, sharePwd, parentId);
	}

	/*
	 * 下载分享文件
	 */
	@RequestMapping(value = "/download", method = RequestMethod.POST)
	public void download(String shareId, String sharePwd, Integer fileId) {

		shareFileService.download(shareId, sharePwd, fileId);
	}

	/**
	 * 保存分享文件到网盘
	 * @param shareId
	 * @param sharePwd
	 * @return
	 */
	@RequestMapping(value = "/saveToCloud", method = RequestMethod.POST)
	@ResponseBody
	public Result saveToCloud(String shareId, String sharePwd, Integer fileId) {

		return shareFileService.saveToCloud(shareId, sharePwd, fileId);
	}

	/**
	 * 创建分享文件
	 * @param param 分享参数
	 * @return
	 */
	@RequestMapping(value="/shareFile", method = RequestMethod.POST)
	@ResponseBody
	public Result shareFile(ShareFileParam param) {
		return shareFileService.shareFile(param);
	}

	/*
	 * 到我分享的文件的列表，进行管理
	 */
	@RequestMapping(value = "/findAll", method = RequestMethod.GET)
	@ResponseBody
	public Result findAll(Page page) {

		return shareFileService.findAll(page);
	}

	/**
	 * 删除我的分享
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/delete/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Result deleteShare(@PathVariable("id")Integer id) {
		return shareFileService.deleteShare(id);
	}

	/**
	 * 获取分享文件媒体信息
	 * @param shareId
	 * @param sharePwd
	 * @param fileId
	 * @return
	 */
	@RequestMapping(value = "/getFileMediaInfo", method = RequestMethod.POST)
	@ResponseBody
	public Result getFileMediaInfo(String shareId, String sharePwd, Integer fileId) {
		return shareFileService.getFileMediaInfo(shareId, sharePwd, fileId);
	}
}
