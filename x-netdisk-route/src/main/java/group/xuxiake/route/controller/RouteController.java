package group.xuxiake.route.controller;

import group.xuxiake.common.entity.FileRecycle;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.route.RouteOfSaveRoutePojo;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;
import group.xuxiake.route.service.RouteService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2020/3/5 23:57.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@RequestMapping("/")
@RestController
public class RouteController {

    @Resource
    private RouteService routeService;

    /**
     * 给用户发送消息
     * @param param
     * @return
     */
    @RequestMapping("/sendMsgToUser")
    public Result sendMsgToUser(@RequestBody RouteOfSendMsgPojo param) {
        return routeService.sendMsgToUser(param);
    }

    @RequestMapping("/sendMsgToGroup")
    public Result sendMsgToGroup() {
        return null;
    }

    /**
     * 保存路由
     * @param param
     * @return
     */
    @RequestMapping("/saveRoute")
    public Result saveRoute(@RequestBody RouteOfSaveRoutePojo param) {
        return routeService.saveRoute(param);
    }

    /**
     * 删除路由
     * @param userId
     * @return
     */
    @RequestMapping("/delRoute")
    public Result delRoute(Integer userId) {
        return routeService.delRoute(userId);
    }

    /**
     * 根据userId查找路由
     * @param userId
     * @return
     */
    @RequestMapping("/findRouteByUser")
    public Result findRouteByUser(Integer userId) {
        return routeService.findRouteByUser(userId);
    }

    /**
     * 获取可用的chat server
     * @param sessionId
     * @return
     */
    @RequestMapping("/getChatServer")
    public Result getChatServer(String sessionId) {
        return routeService.getChatServer(sessionId);
    }

    /**
     * 删除文件
     * @param recycle
     * @return
     */
    @RequestMapping("/delFile")
    public Result delFile(@RequestBody FileRecycle recycle) {
        return routeService.delFile(recycle);
    }


    @RequestMapping("/delJob")
    public Result delJob(@RequestBody FileRecycle recycle) {
        return routeService.delJob(recycle);
    }


}
