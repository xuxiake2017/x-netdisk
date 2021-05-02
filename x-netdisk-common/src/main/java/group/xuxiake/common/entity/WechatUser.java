package group.xuxiake.common.entity;

import group.xuxiake.common.entity.wechat.WechatUserInfo;

import java.util.Date;

public class WechatUser {
    private Integer id;

    private String avatarUrl;

    private String city;

    private String country;

    private Integer gender;

    private String language;

    private String nickName;

    private String province;

    private String openid;

    private Integer userId;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    public WechatUser(WechatUserInfo wechatUserInfo) {
        this.avatarUrl = wechatUserInfo.getAvatarUrl();
        this.city = wechatUserInfo.getCity();
        this.country = wechatUserInfo.getCountry();
        this.gender = wechatUserInfo.getGender();
        this.language = wechatUserInfo.getLanguage();
        this.nickName = wechatUserInfo.getNickName();
        this.province = wechatUserInfo.getProvince();
    }

    public WechatUser() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl == null ? null : avatarUrl.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language == null ? null : language.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province == null ? null : province.trim();
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}