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

    /**
     * post消息给route
     * @param key
     * @param path
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T postMsgToRoute(String key, String path, Object data, Class<T> clazz);
}
