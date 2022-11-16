package group.xuxiake.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import group.xuxiake.admin.service.UserManageService;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.WechatUser;
import group.xuxiake.common.entity.param.UserListQueryParams;
import group.xuxiake.common.entity.param.WeChatUserListQueryParams;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.common.mapper.WechatUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("userManageService")
public class UserManageServiceImpl implements UserManageService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private WechatUserMapper wechatUserMapper;

    @Override
    public Result getUserList(UserListQueryParams queryParams) {
        PageHelper.startPage(queryParams.getPageNum(), queryParams.getPageSize());
        List<User> list = userMapper.getUserList(queryParams);
        list.forEach(user -> user.setNickName(EmojiParser.parseToUnicode(user.getNickName())));
        PageInfo<User> pageInfo = new PageInfo<>(list);
        return new Result(pageInfo);
    }

    @Override
    public Result getWeChatUserList(WeChatUserListQueryParams queryParams) {
        PageHelper.startPage(queryParams.getPageNum(), queryParams.getPageSize());
        List<WechatUser> list = wechatUserMapper.getWeChatUserList(queryParams);
        list.forEach(wechatUser -> wechatUser.setNickName(EmojiParser.parseToUnicode(wechatUser.getNickName())));
        PageInfo<WechatUser> pageInfo = new PageInfo<>(list);
        return new Result(pageInfo);
    }
}
