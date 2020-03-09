package group.xuxiake.common.entity.show;

import lombok.Data;

import java.util.Date;

/**
 * Author by xuxiake, Date on 2019/5/5.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class UserFriendApplyForShowList {

    private Integer id;

    private Integer applicant;

    private Integer respondent;

    private Integer verify;

    private Integer status;

    private String postscript;

    private Date createTime;

    private Date updateTime;

    private String applicantUsername;

    private String applicantAvatar;

    private String respondentUsername;

    private String respondentAvatar;

}
