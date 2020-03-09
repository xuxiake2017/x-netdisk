package group.xuxiake.web.service;

import group.xuxiake.common.entity.Result;

/**
 * Author by xuxiake, Date on 2019/4/30.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public interface TulingRebootService {

    /**
     * 发送消息
     * @param message
     * @return
     */
    Result sendMessage(String message);
}
