package group.xuxiake.web.service.impl;

import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.show.UserFriendApplyForShowList;
import group.xuxiake.common.entity.show.UserFriendNotifyShow;
import group.xuxiake.common.mapper.UserFriendRequestMapper;
import group.xuxiake.web.service.UserFriendNotifyService;
import group.xuxiake.common.entity.Result;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Author by xuxiake, Date on 2019/5/5.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Service("userFriendNotifyService")
public class UserFriendNotifyServiceImpl implements UserFriendNotifyService {

    @Resource
    private UserFriendRequestMapper userFriendRequestMapper;

    /**
     * 获取好友通知
     * @return
     */
    @Override
    public Result getAllNotify() {

        Result result = new Result();
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        List<UserFriendApplyForShowList> list = userFriendRequestMapper.findAll(user.getId());
        List<UserFriendNotifyShow> notifies = new ArrayList<>();
        for (UserFriendApplyForShowList item : list) {
            UserFriendNotifyShow userFriendNotifyShow = new UserFriendNotifyShow();
            userFriendNotifyShow.setType("FRIEND_APPLY_FOR");
            userFriendNotifyShow.setContent(item);
            notifies.add(userFriendNotifyShow);
        }
        result.setData(notifies);
        return result;
    }
}
