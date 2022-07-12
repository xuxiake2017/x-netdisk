package group.xuxiake.common.entity.wechat;

import org.apache.commons.lang.StringUtils;

import com.vdurmont.emoji.EmojiParser;

import lombok.Data;

/**
 * 微信用户信息
 */
@Data
public class WechatUserInfo {
    private String avatarUrl;
    private String city;
    private String country;
    private Integer gender;
    private String language;
    private String nickName;
    private String province;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = StringUtils.isEmpty(nickName) ? nickName : EmojiParser.parseToAliases(nickName.trim());
    }
}
