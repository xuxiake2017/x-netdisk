package group.xuxiake.common.entity.param;

import lombok.Data;

import java.util.Date;

@Data
public class WeChatUserListQueryParams extends CommonPageParams {

    private String searchText;
    private Integer gender;
    private Integer status;
    private Date startTime;
    private Date endTime;
}
