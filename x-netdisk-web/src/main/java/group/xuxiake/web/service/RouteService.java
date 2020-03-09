package group.xuxiake.web.service;

import group.xuxiake.common.entity.RouteShowSimple;

/**
 * Author by xuxiake, Date on 2020/3/5 23:58.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public interface RouteService {

    /**
     * 获取route server
     * @return
     */
    RouteShowSimple getRouteServer(String key);
}
