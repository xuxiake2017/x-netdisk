package group.xuxiake.web.service.impl;

import group.xuxiake.common.entity.SysMessage;
import group.xuxiake.common.mapper.SysMessageMapper;
import group.xuxiake.web.service.MessageService;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.entity.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("messageService")
public class MessageServiceImpl implements MessageService {

    @Resource
    private SysMessageMapper messageMapper;

    @Override
    public Result addMessage(SysMessage message) {
        Result result = new Result();
        messageMapper.insertSelective(message);
        return result;
    }

    /**
     * 消息已读
     * @param id
     * @return
     */
    @Override
    public Result haveRead(Integer id) {
        Result result = new Result();
        if (id == null) {
            result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
            result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.PARAM_IS_NULL));
            return result;
        }
        SysMessage message = new SysMessage();
        message.setId(id);
        message.setStatus(NetdiskConstant.DATA_DELETE_STATUS);
        messageMapper.updateByPrimaryKeySelective(message);
        return result;
    }
}
