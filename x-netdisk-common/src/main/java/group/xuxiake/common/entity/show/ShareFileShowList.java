package group.xuxiake.common.entity.show;

import lombok.Data;

import java.util.Date;

/**
 * Author by xuxiake, Date on 2019/1/18.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class ShareFileShowList {

    private Integer id;

    private String shareId;

    private String sharePwd;

    private Date shareTime;

    private Integer accessTimes;

    private Integer downloadTimes;

    private Integer saveTimes;

    private Integer fileId;

    private String fileKey;

    private String fileName;

    private Long fileSize;

    private Integer fileType;

    private Integer isDir;
}
