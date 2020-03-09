package group.xuxiake.web.service;

import group.xuxiake.common.entity.Message;
import group.xuxiake.common.entity.Result;

public interface MessageService {

    Result addMessage(Message message);

    Result haveRead(Integer id);
}
