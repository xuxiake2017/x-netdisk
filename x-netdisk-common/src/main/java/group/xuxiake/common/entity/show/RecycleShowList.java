package group.xuxiake.common.entity.show;

import lombok.Data;

import java.util.Date;

/**
 * Author by xuxiake, Date on 2019/1/17.
 * PS: Not easy to write code, please indicate.
 * Description：显示回收站文件列表
 */
@Data
public class RecycleShowList {

    // id
    private Integer recycleId;
    // 文件删除时间
    private Date deleteTime;
    // 文件过期时间
    private Date overTime;
    // 文件保存名
    private String key;
    // 文件真实名
    private String fileName;
    // 文件大小
    private Long fileSize;
    // 文件类型
    private Integer fileType;
    // 是否是文件
    private Integer isDir;
}
