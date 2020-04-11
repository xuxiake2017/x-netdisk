package group.xuxiake.common.entity.param;

import lombok.Data;

/**
 * Author by xuxiake, Date on 2020/4/6 18:30.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
public class FileUploadParamByMD5 {

    private String md5Hex;
    private Long fileSize;
    private String fileRealName;
    private Long lastModifiedDate;
    private Integer parentId;
}
