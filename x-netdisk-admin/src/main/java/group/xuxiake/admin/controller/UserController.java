package group.xuxiake.admin.controller;

import group.xuxiake.admin.entity.param.LoginParam;
import group.xuxiake.admin.service.UserService;
import group.xuxiake.common.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginParam loginParam) {
        return userService.login(loginParam);
    }

    @RequestMapping("/getCaptcha")
    public Result login() {
        return userService.getCaptcha();
    }

    @RequestMapping("/getInfo")
    public Result getInfo() {
        return userService.getInfo();
    }
}
