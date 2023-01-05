package group.xuxiake.common.entity.param;

import lombok.Data;

import java.util.Date;

@Data
public class UserListQueryParams extends CommonPageParams {

    private String searchText;
    private Integer userStatus;
    private Integer sex;
    private Date startTime;
    private Date endTime;
}
