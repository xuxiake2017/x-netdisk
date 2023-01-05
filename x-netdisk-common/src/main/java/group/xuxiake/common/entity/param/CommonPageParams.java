package group.xuxiake.common.entity.param;

import lombok.Data;

@Data
public class CommonPageParams {
    private Integer pageNum;
    private Integer pageSize;

    public CommonPageParams() {
        this.pageNum = 1;
        this.pageSize = 10;
    }
}
