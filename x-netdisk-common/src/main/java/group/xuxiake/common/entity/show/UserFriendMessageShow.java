package group.xuxiake.common.entity.show;

import lombok.Data;

import java.util.Date;

/**
 * @author: xuxiake
 * @create: 2019-05-04 19:03
 * @description:
 **/
@Data
public class UserFriendMessageShow {

    private Integer id;

    private Integer from;

    private Integer to;

    private String content;

    private Integer fileId;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Integer userId;

    private String userAvatar;

    private String userName;

    private Integer friendId;

    private String friendAvatar;

    private String friendName;
}
