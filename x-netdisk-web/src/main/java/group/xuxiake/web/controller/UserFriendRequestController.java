package group.xuxiake.web.controller;

import group.xuxiake.common.entity.UserFriendRequest;
import group.xuxiake.common.entity.Result;
import group.xuxiake.web.service.UserFriendRequestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2019/5/5.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@RestController
@RequestMapping("/friendRequest")
public class UserFriendRequestController {

    @Resource
    private UserFriendRequestService userFriendRequestService;

    /**
     * 好友申请操作
     * @param applicant 申请者id
     * @param option 操作 1：同意；2：拒绝
     * @return
     */
    @RequestMapping("/friendRequestOption")
    public Result friendApplyForOption(Integer applicant, Integer option) {

        return userFriendRequestService.friendRequestOption(applicant, option);
    }

    /**
     * 搜索好友
     * @param key 关键词
     * @return
     */
    @RequestMapping("/searchFriend")
    public Result searchFriend(String key) {

        return userFriendRequestService.searchFriend(key);
    }

    /**
     * 添加好友请求
     * @param param
     * @return
     */
    @RequestMapping("/addFriendRequest")
    public Result addFriendRequest(UserFriendRequest param) {
        return userFriendRequestService.addFriendRequest(param);
    }
}
