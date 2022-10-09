package group.xuxiake.admin.security.handle;

import group.xuxiake.admin.security.entity.LoginUser;
import group.xuxiake.common.entity.UserAdmin;
import group.xuxiake.common.mapper.UserAdminMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户验证处理
 */
@Component("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService
{
    @Resource
    private UserAdminMapper userAdminMapper;
    @Resource
    private SysPasswordService sysPasswordService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        UserAdmin userAdmin = userAdminMapper.selectByUsername(username);
        if (userAdmin == null) {
            throw new UsernameNotFoundException("登录用户：" + username + " 不存在");
        }
        sysPasswordService.validate(userAdmin);
        return new LoginUser(userAdmin);
    }
}
