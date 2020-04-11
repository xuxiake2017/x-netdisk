package group.xuxiake.web.service;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.SysMessage;

public interface MessageService {

    Result addMessage(SysMessage message);

    Result haveRead(Integer id);
}
