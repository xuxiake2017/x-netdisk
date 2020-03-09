package group.xuxiake.web.controller;

import group.xuxiake.web.service.UserFriendNotifyService;
import group.xuxiake.common.entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2019/5/5.
 * PS: Not easy to write code, please indicate.
 * Description：好友通知
 */
@RestController
@RequestMapping("/friendNotify")
public class UserFriendNotifyController {

    @Resource
    private UserFriendNotifyService userFriendNotifyService;

    /**
     * 获取好友通知
     * @return
     */
    @RequestMapping("/getAll")
    public Result getAllNotify() {
        return userFriendNotifyService.getAllNotify();
    }
}
