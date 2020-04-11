package group.xuxiake.common.entity.show;

import group.xuxiake.common.entity.FileMedia;
import group.xuxiake.common.entity.FileOrigin;
import lombok.Data;

import java.util.Date;

/**
 * Author by xuxiake, Date on 2020/4/6 10:41.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class FileShowMedia {

    private Integer id;

    private Integer originId;

    private String fileName;

    private String filePath;

    private Integer isDir;

    private Integer userId;

    private Integer parentId;

    private String key;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private FileMedia fileMedia;

    private FileOrigin fileOrigin;
}
