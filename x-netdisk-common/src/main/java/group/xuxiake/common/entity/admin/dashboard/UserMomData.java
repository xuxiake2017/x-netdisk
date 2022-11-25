package group.xuxiake.common.entity.admin.dashboard;

import lombok.Data;

/**
 * @Author xuxiake
 * @Date 16:06 2022/11/17
 * @Description 用户新增环比数据
 */
@Data
public class UserMomData {
    // 今日新增用户数
    private Integer todayIncreasedUserNum;
    // 日环比数据
    private Integer dayMomData;
    // 本周新增用户数
    private Integer currentWeekIncreasedUserNum;
    // 周环比数据
    private Integer weekMomData;
    // 周环比数据
    private Integer totalUserNum;

    public UserMomData(Integer todayIncreasedUserNum, Integer dayMomData, Integer currentWeekIncreasedUserNum, Integer weekMomData, Integer totalUserNum) {
        this.todayIncreasedUserNum = todayIncreasedUserNum;
        this.dayMomData = dayMomData;
        this.currentWeekIncreasedUserNum = currentWeekIncreasedUserNum;
        this.weekMomData = weekMomData;
        this.totalUserNum = totalUserNum;
    }

    public UserMomData() {
    }
}
