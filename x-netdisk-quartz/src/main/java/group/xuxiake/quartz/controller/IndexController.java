package group.xuxiake.quartz.controller;

import group.xuxiake.common.entity.Recycle;
import group.xuxiake.common.entity.Result;
import group.xuxiake.quartz.service.IndexService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2020/3/30 10:15.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@RestController
@RequestMapping("/")
public class IndexController {

    @Resource
    private IndexService indexService;

    /**
     * 删除文件（创建定时清理过期文件的任务）
     * @param recycle
     * @return
     */
    @RequestMapping("/delFile")
    public Result delFile(@RequestBody Recycle recycle) {
        return indexService.delFile(recycle);
    }

    /**
     * 删除定时清理过期文件的任务
     * @param recycle
     * @return
     */
    @RequestMapping("/delJob")
    public Result delJob(@RequestBody Recycle recycle) {
        return indexService.delJob(recycle);
    }
}
