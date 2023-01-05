package group.xuxiake.admin.service;

import group.xuxiake.admin.entity.param.LoginParam;
import group.xuxiake.common.entity.Result;

public interface UserService {
    Result login(LoginParam loginParam);

    Result getCaptcha();

    Result getInfo();
}
