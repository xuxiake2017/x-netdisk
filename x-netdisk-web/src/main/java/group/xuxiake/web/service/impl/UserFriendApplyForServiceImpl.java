package group.xuxiake.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import group.xuxiake.common.entity.UserFriendApplyFor;
import group.xuxiake.common.entity.UserFriendList;
import group.xuxiake.common.entity.UserFriendMessage;
import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.entity.chat.ChatMessageBase;
import group.xuxiake.common.entity.show.UserFriendApplyForShowList;
import group.xuxiake.common.entity.show.UserFriendListShow;
import group.xuxiake.common.entity.show.UserFriendMessageShow;
import group.xuxiake.common.entity.show.UserShowSimple;
import group.xuxiake.common.mapper.UserFriendApplyForMapper;
import group.xuxiake.common.mapper.UserFriendListMapper;
import group.xuxiake.common.mapper.UserFriendMessageMapper;
import group.xuxiake.common.mapper.UserNetdiskMapper;
import group.xuxiake.web.service.UserFriendApplyForService;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Author by xuxiake, Date on 2019/5/5.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Service("userFriendApplyForService")
public class UserFriendApplyForServiceImpl implements UserFriendApplyForService {

    @Resource
    private UserFriendApplyForMapper userFriendApplyForMapper;
    @Resource
    private UserFriendListMapper userFriendListMapper;
    @Resource
    private UserNetdiskMapper userNetdiskMapper;
    @Resource
    private UserFriendMessageMapper userFriendMessageMapper;

    /**
     * 好友申请操作
     * @param applicant 申请者id
     * @param option 操作 1：同意；2：拒绝
     * @return
     */
    @Override
    public Result friendApplyForOption(Integer applicant, Integer option) {

        Result result = new Result();
        if (applicant == null
                || option == null
                || (option != NetdiskConstant.FRIEND_APPLY_FOR_OPTION_OF_AGREE && option != NetdiskConstant.FRIEND_APPLY_FOR_OPTION_OF_REFUSE)) {
            return Result.paramIsNull(result);
        }
        UserNetdisk user = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        UserNetdisk friend = userNetdiskMapper.selectByPrimaryKey(applicant);
        userFriendApplyForMapper.friendApplyForOption(applicant, user.getId(), option);
        if (option == NetdiskConstant.FRIEND_APPLY_FOR_OPTION_OF_AGREE) {
            // 同意好友

            UserFriendMessageShow userFriendMessage = new UserFriendMessageShow();
            userFriendMessage.setFrom(user.getId());
            userFriendMessage.setTo(friend.getId());
            userFriendMessage.setContent("我已同意你的好友请求");
            userFriendMessage.setCreateTime(new Date());
            // user friend需要对调
            userFriendMessage.setUserId(friend.getId());
            userFriendMessage.setUserAvatar(friend.getAvatar());
            userFriendMessage.setUserName(friend.getUsername());
            userFriendMessage.setFriendId(user.getId());
            userFriendMessage.setFriendAvatar(user.getAvatar());
            userFriendMessage.setFriendName(user.getUsername());

            ChatMessageBase chatMessageBase = new ChatMessageBase();
            chatMessageBase.setCreateTime(new Date());
            chatMessageBase.setContent(userFriendMessage);
            chatMessageBase.setType("FRIEND");
//            ChatWebSocketHandler.sendMessage(userFriendMessage.getTo(), JSONObject.toJSONString(chatMessageBase));

            // 保存聊天记录
            UserFriendMessage userFriendMessage_ = new UserFriendMessage();
            userFriendMessage_.setContent(userFriendMessage.getContent());
            userFriendMessage_.setFrom(userFriendMessage.getFrom());
            userFriendMessage_.setTo(userFriendMessage.getTo());
            userFriendMessageMapper.insertSelective(userFriendMessage_);

            UserFriendList userFriendList = new UserFriendList();
            userFriendList.setUserId(user.getId());
            userFriendList.setFriendId(applicant);
            userFriendListMapper.insertSelective(userFriendList);
            userFriendList.setUserId(applicant);
            userFriendList.setFriendId(user.getId());
            userFriendListMapper.insertSelective(userFriendList);
        }
        return result;
    }

    /**
     * 搜索好友
     * @param key 关键词
     * @return
     */
    @Override
    public Result searchFriend(String key) {

        Result result = new Result();
        if (StringUtils.isAnyEmpty(key)) {
            return Result.paramIsNull(result);
        }
        UserNetdisk user = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        List<UserShowSimple> users = userNetdiskMapper.searchFriend(key, user.getId());
        result.setData(users);
        return result;
    }

    /**
     * 添加好友请求
     * @param param
     * @return
     */
    @Override
    public Result addFriendRequest(UserFriendApplyFor param) {

        Result result = new Result();
        if (param.getRespondent() == null) {
            return Result.paramIsNull(result);
        }
        UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
        List<UserFriendListShow> friendList = userFriendListMapper.getFriendList(userNetdisk.getId());
        for (UserFriendListShow item : friendList) {
            if (item.getFriendId().intValue() == param.getRespondent().intValue()) {
                // 好友已存在
                result.setCode(NetdiskErrMsgConstant.ADD_FRIEND_REQUEST_FRIEND_IS_EXIST);
                return result;
            }
        }
        param.setApplicant(userNetdisk.getId());

        UserNetdisk respondent = userNetdiskMapper.selectByPrimaryKey(param.getRespondent());

        UserFriendApplyForShowList userFriendApplyForShowList = new UserFriendApplyForShowList();
        userFriendApplyForShowList.setApplicant(userNetdisk.getId());
        userFriendApplyForShowList.setApplicantAvatar(userNetdisk.getAvatar());
        userFriendApplyForShowList.setApplicantUsername(userNetdisk.getUsername());
        userFriendApplyForShowList.setRespondent(respondent.getId());
        userFriendApplyForShowList.setRespondentAvatar(respondent.getAvatar());
        userFriendApplyForShowList.setRespondentUsername(respondent.getUsername());
        userFriendApplyForShowList.setPostscript(param.getPostscript());

        ChatMessageBase chatMessageBase = new ChatMessageBase();
        chatMessageBase.setType("FRIEND_APPLY_FOR");
        chatMessageBase.setCreateTime(new Date());
        chatMessageBase.setContent(userFriendApplyForShowList);
        // websocket发送请求
//        ChatWebSocketHandler.sendMessage(userFriendApplyForShowList.getRespondent(), JSONObject.toJSONString(chatMessageBase));

        // 插入数据库
        userFriendApplyForMapper.insertSelective(param);
        return result;
    }
}
