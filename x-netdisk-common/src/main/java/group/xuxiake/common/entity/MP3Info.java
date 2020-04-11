package group.xuxiake.common.entity;

import lombok.Data;

/**
 * Author by xuxiake, Date on 2020/4/6 16:58.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class MP3Info {

    // 比特率
    private Integer bitrate;
    // 歌曲名字
    private String title;
    // 歌手名字
    private String artist;
    // 时长（秒）
    private Long duration;
    // 专辑照片
    private byte[] albumImage;
}
