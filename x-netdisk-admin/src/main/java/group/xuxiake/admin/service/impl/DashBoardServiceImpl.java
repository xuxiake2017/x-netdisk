package group.xuxiake.admin.service.impl;

import group.xuxiake.admin.service.DashBoardService;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.admin.dashboard.*;
import group.xuxiake.common.mapper.*;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author xuxiake
 * @Date 17:12 2022/11/16
 * @Description
 */
@Service("dashBoardService")
public class DashBoardServiceImpl implements DashBoardService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private SysLogMapper sysLogMapper;
    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private SmsLogMapper smsLogMapper;
    @Resource
    private FileOriginMapper fileOriginMapper;

    /**
     * 获取用户新增环比数据
     * @return
     */
    private UserMomData getUserMomData() {
        DateTime dt = new DateTime();
        // 今日新增用户数
        Integer todayIncreasedUserNum = userMapper.getTimePeriodUserNum(dt.dayOfWeek().roundFloorCopy().toDate(), dt.toDate());
        // 日环比数据
        Integer dayMomData = userMapper.getTimePeriodUserNum(dt.dayOfWeek().roundFloorCopy().minusDays(1).toDate(), dt.dayOfWeek().roundCeilingCopy().minusDays(1).toDate());
        // 本周新增用户数
        Integer currentWeekIncreasedUserNum = userMapper.getTimePeriodUserNum(dt.weekOfWeekyear().roundFloorCopy().toDate(), dt.toDate());
        // 周环比数据
        Integer weekMomData = userMapper.getTimePeriodUserNum(dt.weekOfWeekyear().roundFloorCopy().minusDays(7).toDate(), dt.weekOfWeekyear().roundCeilingCopy().minusDays(7).toDate());
        // 用户总数
        Integer totalUserNum = userMapper.getTimePeriodUserNum(new DateTime(0).toDate(), dt.toDate());
        return new UserMomData(todayIncreasedUserNum, dayMomData, currentWeekIncreasedUserNum, weekMomData, totalUserNum);
    }

    /**
     * 获取访问量数据
     * @return
     */
    private PVData getPVData() {
        DateTime dt = new DateTime();
        // 日访问量
        Integer todayPVNum = sysLogMapper.getTimePeriodPVNum(dt.dayOfWeek().roundFloorCopy().toDate(), dt.toDate());
        // 最近一个月访问量
        Integer currentMonthPVNum = sysLogMapper.getTimePeriodPVNum(dt.minusDays(30).toDate(), dt.toDate());
        // 最近一个月访问量统计
        List<StatisticsDataItem> statisticsData = sysLogMapper.getTimePeriodStatisticsData(dt.minusDays(30).toDate(), dt.toDate(), "day");
        return new PVData(todayPVNum, currentMonthPVNum, statisticsData);
    }

    /**
     * 获取上传量数据
     * @return
     */
    private UploadData getUploadData() {
        DateTime dt = new DateTime();
        // 日上传数量
        Integer todayUploadNum = userFileMapper.getTimePeriodUploadNum(dt.dayOfWeek().roundFloorCopy().toDate(), dt.toDate());
        // 最近一个月上传数量
        Integer currentMonthUploadNum = userFileMapper.getTimePeriodUploadNum(dt.minusDays(30).toDate(), dt.toDate());
        // 最近一个月上传数量统计
        List<StatisticsDataItem> statisticsData = userFileMapper.getTimePeriodStatisticsData(dt.minusDays(30).toDate(), dt.toDate(), "day");
        return new UploadData(todayUploadNum, currentMonthUploadNum, statisticsData);
    }

    /**
     * 获取短信发送量数据
     * @return
     */
    private SmsSendData getSmsSendData() {
        DateTime dt = new DateTime();
        // 日上传数量
        Integer todaySendNum = smsLogMapper.getTimePeriodSendNum(dt.dayOfWeek().roundFloorCopy().toDate(), dt.toDate());
        // 最近一个月发送数量
        Integer currentMonthSendNum = smsLogMapper.getTimePeriodSendNum(dt.minusDays(30).toDate(), dt.toDate());
        // 最近一个月发送数量统计
        List<StatisticsDataItem> statisticsData = smsLogMapper.getTimePeriodStatisticsData(dt.minusDays(30).toDate(), dt.toDate(), "day");
        return new SmsSendData(todaySendNum, currentMonthSendNum, statisticsData);
    }

    private String getGroupType(Date startTime, Date endTime) {
        final long oneDayTimestamp = 24 * 60 * 60 * 1000L;
        long timePeriod = endTime.getTime() - startTime.getTime();
        if (timePeriod <= 2 * oneDayTimestamp) {
            return "hour";
        } else if (timePeriod < 31 * oneDayTimestamp) {
            return  "day";
        } else {
            return "month";
        }
        
    }

    @Override
    public Result getCardData() {
        Result result = new Result();
        CardData cardData = new CardData();
        cardData.setUserMomData(this.getUserMomData());
        cardData.setPvData(this.getPVData());
        cardData.setUploadData(this.getUploadData());
        cardData.setSmsSendData(this.getSmsSendData());
        result.setData(cardData);
        return result;
    }

    @Override
    public Result getStatisticsData(StatisticsDataQueryParam param) {
        DateTime dt = new DateTime();
        Date startTime = param.getStartTime();
        Date endTime = param.getEndTime();
        if (startTime == null || endTime == null) {
            startTime = dt.minusDays(30).toDate();
            endTime = dt.toDate();
        }
        String groupType = this.getGroupType(startTime, endTime);
        List<StatisticsDataItem> userStatisticsData = userMapper.getTimePeriodStatisticsData(startTime, endTime, groupType);
        List<StatisticsDataItem> uploadStatisticsData = userFileMapper.getTimePeriodStatisticsData(startTime, endTime, groupType);
        List<StatisticsDataItem> uploadSizeStatisticsData = fileOriginMapper.getTimePeriodStatisticsData(startTime, endTime, groupType);
        List<StatisticsDataItem> smsStatisticsData = smsLogMapper.getTimePeriodStatisticsData(startTime, endTime, groupType);
        return new Result(new StatisticsData(userStatisticsData, uploadStatisticsData, uploadSizeStatisticsData, smsStatisticsData));
    }
}
