package group.xuxiake.common.entity;

import java.io.Serializable;
import java.util.Date;

public class FileMedia implements Serializable {
    private Integer id;

    private Integer originId;

    private Date shootTime;

    private Integer imgWidth;

    private Integer imgHeight;

    private Integer videoWidth;

    private Integer videoHeight;

    private Integer videoDuration;

    private String thumbnailUrl;

    private String musicPoster;

    private String musicArtist;

    private String reserveColumn1;

    private String reserveColumn2;

    private String reserveColumn3;

    private String reserveColumn4;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOriginId() {
        return originId;
    }

    public void setOriginId(Integer originId) {
        this.originId = originId;
    }

    public Date getShootTime() {
        return shootTime;
    }

    public void setShootTime(Date shootTime) {
        this.shootTime = shootTime;
    }

    public Integer getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(Integer imgWidth) {
        this.imgWidth = imgWidth;
    }

    public Integer getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(Integer imgHeight) {
        this.imgHeight = imgHeight;
    }

    public Integer getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(Integer videoWidth) {
        this.videoWidth = videoWidth;
    }

    public Integer getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(Integer videoHeight) {
        this.videoHeight = videoHeight;
    }

    public Integer getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(Integer videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl == null ? null : thumbnailUrl.trim();
    }

    public String getMusicPoster() {
        return musicPoster;
    }

    public void setMusicPoster(String musicPoster) {
        this.musicPoster = musicPoster == null ? null : musicPoster.trim();
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist == null ? null : musicArtist.trim();
    }

    public String getReserveColumn1() {
        return reserveColumn1;
    }

    public void setReserveColumn1(String reserveColumn1) {
        this.reserveColumn1 = reserveColumn1 == null ? null : reserveColumn1.trim();
    }

    public String getReserveColumn2() {
        return reserveColumn2;
    }

    public void setReserveColumn2(String reserveColumn2) {
        this.reserveColumn2 = reserveColumn2 == null ? null : reserveColumn2.trim();
    }

    public String getReserveColumn3() {
        return reserveColumn3;
    }

    public void setReserveColumn3(String reserveColumn3) {
        this.reserveColumn3 = reserveColumn3 == null ? null : reserveColumn3.trim();
    }

    public String getReserveColumn4() {
        return reserveColumn4;
    }

    public void setReserveColumn4(String reserveColumn4) {
        this.reserveColumn4 = reserveColumn4 == null ? null : reserveColumn4.trim();
    }
}