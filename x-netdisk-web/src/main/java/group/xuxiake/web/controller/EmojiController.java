package group.xuxiake.web.controller;

import group.xuxiake.web.service.EmojiService;
import group.xuxiake.common.entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: xuxiake
 * @create: 2019-05-08 22:52
 * @description:
 **/
@RestController
@RequestMapping("/emoji")
public class EmojiController {

    @Resource
    private EmojiService emojiService;

    @RequestMapping(value = "/parseToHtmlDecimal", method = RequestMethod.POST)
    public Result parseToHtmlDecimal(String aliase) {

        return emojiService.parseToHtmlDecimal(aliase);
    }
}
