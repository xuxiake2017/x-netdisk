package group.xuxiake.admin.controller;

import group.xuxiake.admin.service.DashBoardService;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.admin.dashboard.StatisticsDataQueryParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author xuxiake
 * @Date 17:09 2022/11/16
 * @Description
 */
@RestController
@RequestMapping("/dashBoard")
public class DashBoardController {

    @Resource
    private DashBoardService dashBoardService;

    @GetMapping("/getCardData")
    public Result getCardData() {
        return dashBoardService.getCardData();
    }

    @PostMapping("/getStatisticsData")
    public Result getStatisticsData(@RequestBody StatisticsDataQueryParam param) {
        return dashBoardService.getStatisticsData(param);
    }
}
