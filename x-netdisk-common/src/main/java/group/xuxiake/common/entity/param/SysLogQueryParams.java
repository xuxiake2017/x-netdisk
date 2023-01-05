package group.xuxiake.common.entity.param;

import lombok.Data;

import java.util.Date;

@Data
public class SysLogQueryParams extends CommonPageParams {

    private String searchText;
    private Integer clientType;
    private Integer logType;
    private Date startTime;
    private Date endTime;
}
