package group.xuxiake.web.controller;

import group.xuxiake.common.entity.Page;
import group.xuxiake.web.service.RecycleService;
import group.xuxiake.common.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 回收站的Controller
 * @author xuxiake
 *
 */
@Controller
@RequestMapping("/recycle")
public class RecycleController {

	@Resource
	private RecycleService recycleService;
	
	/*
	 * 得到回收站列表
	 */
	@RequestMapping("/toRecycleList")
	@ResponseBody
	public Result toRecycleList(Page page) {
		return recycleService.toRecycleList(page);
	}

	/**
	 * 永久删除文件
	 * 	由于隔代机制，父级目录的status是1，就算其子目录的status是2，也不会再被检索到，所以不需要把子目录的status变为1
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/delete/{id}")
	@ResponseBody
	public Result delete(@PathVariable("id")Integer id) {
		return recycleService.delete(id);
	}

	/**
	 * 恢复文件
	 * @param recycleId
	 * @param fileSaveName
	 * @return
	 */
	@RequestMapping(value="/reback")
	@ResponseBody
	public Result reback(Integer recycleId, String fileSaveName) {
		return recycleService.reback(recycleId, fileSaveName);
	}
}
