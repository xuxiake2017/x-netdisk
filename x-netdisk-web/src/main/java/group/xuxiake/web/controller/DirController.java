package group.xuxiake.web.controller;

import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.entity.Result;
import group.xuxiake.web.service.FileService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 用于文件夹操作的Controller
 * @author xuxiake
 *
 */
@Controller
@RequestMapping("/dir")
public class DirController {

	@Resource
	protected FileService fileService;

	/**
	 * 列出所有文件夹，用于文件移动
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value="/listAllDir")
	@ResponseBody
	public Result listAllDir(Integer parentId) {
		return fileService.listAllDir(parentId);
	}

	/**
	 * 移动文件
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/moveFile")
	@ResponseBody
	public Result moveFile(UserFile param) {
		return fileService.moveFile(param);
	}

	/**
	 * 创建新的文件夹
	 * @param userFile
	 * @return
	 */
	@RequestMapping(value="/mkDir")
	@ResponseBody
	public Result mkDir(UserFile userFile) {
		
		return fileService.mkDir(userFile);
	}
}
