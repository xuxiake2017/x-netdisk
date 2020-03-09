package group.xuxiake.common.entity.route;

import lombok.Data;

/**
 * Author by xuxiake, Date on 2020/3/6 21:52.
 * PS: Not easy to write code, please indicate.
 * Description：保存路由pojo
 */
@Data
public class RouteOfSaveRoutePojo {

    private Integer userId;
    private String ip;
    private String httpPort;
}
