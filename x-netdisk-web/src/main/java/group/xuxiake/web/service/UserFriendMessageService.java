package group.xuxiake.web.service;

import group.xuxiake.common.entity.Page;
import group.xuxiake.common.entity.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

public interface UserFriendMessageService {

    /**
     *
     * @return
     */
    Result getFriendMessages();

    /**
     * 获取好友消息列表
     * @param friendId
     * @param page
     * @return
     */
    Result getFriendMessages(Page page, Integer friendId);

    /**
     * 图片上传
     * @param file
     * @return
     */
    Result uploadImage(MultipartFile file);

    /**
     * 获取可用的chat server
     * @param session
     * @return
     */
    Result getServer(HttpSession session);
}
