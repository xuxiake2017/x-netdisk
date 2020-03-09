package group.xuxiake.web.controller;

import group.xuxiake.web.service.TulingRebootService;
import group.xuxiake.common.entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2019/4/30.
 * PS: Not easy to write code, please indicate.
 * Description：图灵聊天机器人
 */
@RestController
@RequestMapping("/tuling")
public class TulingRebootController {

    @Resource
    private TulingRebootService tulingRebootService;

    /**
     * 发送消息
     * @param message
     * @return
     */
    @RequestMapping("/sendMessage")
    public Result sendMessage(String message) {
        return tulingRebootService.sendMessage(message);
    }
}
