package group.xuxiake.admin.service;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.param.UserListQueryParams;
import group.xuxiake.common.entity.param.WeChatUserListQueryParams;

public interface UserManageService {
    Result getUserList(UserListQueryParams queryParams);

    Result getWeChatUserList(WeChatUserListQueryParams queryParams);
}
