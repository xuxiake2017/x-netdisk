package group.xuxiake.common.entity.param;

import lombok.Data;

import java.util.Date;

@Data
public class SmsLogQueryParams extends CommonPageParams {

    private String searchText;
    private Integer clientType;
    private Integer success;
    private Date startTime;
    private Date endTime;
}
