package group.xuxiake.quartz.job;

import com.google.gson.Gson;
import group.xuxiake.common.entity.Recycle;
import group.xuxiake.common.mapper.RecycleMapper;
import group.xuxiake.common.util.NetdiskConstant;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * Author by xuxiake, Date on 2019/12/13.
 * PS: Not easy to write code, please indicate.
 * Description：删除回收站文件job
 */
@Slf4j
public class QuartzDelRecycleFileJob extends QuartzJobBean implements Serializable {

    @Resource
    private RecycleMapper recycleMapper;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {

        log.info("QuartzDelFileJob executeInternal");
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        String data = (String) jobDetail.getJobDataMap().get("data");
        Recycle recycle = new Gson().fromJson(data, Recycle.class);
        recycle.setRecycleStatus(NetdiskConstant.RECYCLE_STATUS_FILE_HAVE_BEEN_DEL_FOREVER + "");
        recycleMapper.updateByPrimaryKeySelective(recycle);
    }
}
