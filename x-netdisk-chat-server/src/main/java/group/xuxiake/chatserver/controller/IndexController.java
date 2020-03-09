package group.xuxiake.chatserver.controller;

import group.xuxiake.chatserver.service.IndexService;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2020/3/7 21:41.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@RestController
@RequestMapping("/")
public class IndexController {

    @Resource
    private IndexService indexService;

    /**
     * 给好友发送消息
     * @param param
     * @return
     */
    @RequestMapping("/sendMsg")
    public Result sendMsg(@RequestBody RouteOfSendMsgPojo param) {
        return indexService.sendMsg(param);
    }
}
