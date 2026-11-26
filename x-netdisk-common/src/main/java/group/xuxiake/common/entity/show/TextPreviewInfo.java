package group.xuxiake.common.entity.show;

import lombok.Data;

@Data
public class TextPreviewInfo {

    private String fileKey;
    private String fileName;
    private String fileExtName;
    private Long fileSize;
    private String encoding;
    private String content;
}
