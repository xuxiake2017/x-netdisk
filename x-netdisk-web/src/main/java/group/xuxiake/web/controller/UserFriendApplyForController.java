package group.xuxiake.web.controller;

import group.xuxiake.common.entity.UserFriendApplyFor;
import group.xuxiake.web.service.UserFriendApplyForService;
import group.xuxiake.common.entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2019/5/5.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@RestController
@RequestMapping("/friendApplyFor")
public class UserFriendApplyForController {

    @Resource
    private UserFriendApplyForService userFriendApplyForService;

    /**
     * 好友申请操作
     * @param applicant 申请者id
     * @param option 操作 1：同意；2：拒绝
     * @return
     */
    @RequestMapping("/friendApplyForOption")
    public Result friendApplyForOption(Integer applicant, Integer option) {

        return userFriendApplyForService.friendApplyForOption(applicant, option);
    }

    /**
     * 搜索好友
     * @param key 关键词
     * @return
     */
    @RequestMapping("/searchFriend")
    public Result searchFriend(String key) {

        return userFriendApplyForService.searchFriend(key);
    }

    /**
     * 添加好友请求
     * @param param
     * @return
     */
    @RequestMapping("/addFriendRequest")
    public Result addFriendRequest(UserFriendApplyFor param) {
        return userFriendApplyForService.addFriendRequest(param);
    }
}
