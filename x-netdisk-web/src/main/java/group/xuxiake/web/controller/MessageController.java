package group.xuxiake.web.controller;

import group.xuxiake.web.service.MessageService;
import group.xuxiake.common.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;
    /**
     * 消息已读
     * @return
     */
    @RequestMapping("/haveRead")
    @ResponseBody
    public Result haveRead(Integer id) {
        return messageService.haveRead(id);
    }
}
