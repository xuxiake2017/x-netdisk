package group.xuxiake.route.service;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.route.RouteOfSaveRoutePojo;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;

import javax.servlet.http.HttpSession;

/**
 * Author by xuxiake, Date on 2020/3/5 23:58.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
public interface RouteService {

    /**
     * 保存路由
     * @param param
     * @return
     */
    Result saveRoute(RouteOfSaveRoutePojo param);

    /**
     * 删除路由
     * @param userId
     * @return
     */
    Result delRoute(Integer userId);

    /**
     * 给用户发送消息
     * @param param
     * @return
     */
    Result sendMsgToUser(RouteOfSendMsgPojo param);

    /**
     * 根据userId查找路由
     * @param userId
     * @return
     */
    Result findRouteByUser(Integer userId);

    /**
     * 获取可用的chat server
     * @param sessionId
     * @return
     */
    Result getChatServer(String sessionId);
}
