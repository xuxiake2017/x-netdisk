package group.xuxiake.admin.controller;

import group.xuxiake.admin.entity.param.LoginParam;
import group.xuxiake.admin.service.UserManageService;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.param.UserListQueryParams;
import group.xuxiake.common.entity.param.WeChatUserListQueryParams;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/userManage")
public class UserManageController {

    @Resource
    private UserManageService userManageService;

    @PostMapping("/getUserList")
    public Result getUserList(@RequestBody UserListQueryParams queryParams) {
        return userManageService.getUserList(queryParams);
    }

    @PostMapping("/getWeChatUserList")
    public Result getWeChatUserList(@RequestBody WeChatUserListQueryParams queryParams) {
        return userManageService.getWeChatUserList(queryParams);
    }
}
