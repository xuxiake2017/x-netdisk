package group.xuxiake.web.controller;

import group.xuxiake.common.entity.Page;
import group.xuxiake.web.service.UserFriendMessageService;
import group.xuxiake.common.entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author: xuxiake
 * @create: 2019-05-01 14:44
 * @description:
 **/
@RestController
@RequestMapping("/friendMessage")
public class UserFriendMessageController {

    @Resource
    private UserFriendMessageService userFriendMessageService;

    /**
     * 获取好友消息列表
     * @return
     */
    @RequestMapping("/getFriendMessages")
    public Result getFriendMessages(Page page, Integer friendId) {

        if (friendId == null) {
            return userFriendMessageService.getFriendMessages();
        } else {
            return userFriendMessageService.getFriendMessages(page, friendId);
        }

    }

    /**
     * 图片上传
     * @param file
     * @return
     */
    @RequestMapping("/uploadImage")
    public Result uploadImage(MultipartFile file) {
        return userFriendMessageService.uploadImage(file);
    }

    /**
     * 获取可用的chat server
     * @param session
     * @return
     */
    @RequestMapping("/getServer")
    public Result getServer(HttpSession session) {
        return userFriendMessageService.getServer(session);
    }
}
