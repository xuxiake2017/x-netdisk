package group.xuxiake.web.handler;

import group.xuxiake.common.entity.Recycle;
import group.xuxiake.common.mapper.RecycleMapper;
import group.xuxiake.common.util.NetdiskConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author by xuxiake, Date on 2019/1/18.
 * PS: Not easy to write code, please indicate.
 * Description：处理回收站清理
 */
@Slf4j
@Component
public class RecycleHandler {

    // 回收站文件内存库，以回收站id为key
    public static Map<Integer, Recycle> recycles = new ConcurrentHashMap<>();
    @Resource
    private RecycleMapper recycleMapper;

    /**
     * 初始化
     */
    public void init() {
        log.debug("回收站内存库初始化");
        if (recycles.size() == 0) {
            List<Recycle> recycleList = recycleMapper.findAll();
            if (recycleList.size() > 0) {
                for (Recycle recycle : recycleList) {
                    recycles.put(recycle.getRecycleId(), recycle);
                }
            }
        }
    }

    /**
     * 核心工作
     */
    public void framework() {

        Set<Map.Entry<Integer, Recycle>> entries = recycles.entrySet();
        if (entries.size() > 0) {
            for (Map.Entry<Integer, Recycle> entry : entries) {
                Integer key = entry.getKey();
                Recycle value = entry.getValue();
                if (this.isOutOfDate(value)) {
                    this.clearOutOfDateFile(key);
                }
            }
        }
    }

    /**
     * 向内存库添加文件
     * @param recycle
     */
    public static void put(Recycle recycle) {
        log.debug("回收站内存库添加文件：{}", recycle);
        recycles.put(recycle.getRecycleId(), recycle);
    }

    /**
     * 从内存库移除文件
     * @param recycleId
     */
    public static void remove(Integer recycleId) {
        log.debug("回收站内存库移除文件：{}", recycleId);
        recycles.remove(recycleId);
    }

    /**
     * 清理过期文件
     * @param recycleId 回收站id
     */
    public void clearOutOfDateFile(Integer recycleId) {

        log.debug("回收站文件：{}已被定时清理", recycleId);
        Recycle recycle = new Recycle();
        recycle.setRecycleId(recycleId);
        recycle.setRecycleStatus(NetdiskConstant.RECYCLE_STATUS_FILE_HAVE_BEEN_DEL_FOREVER + "");
        recycleMapper.updateByPrimaryKeySelective(recycle);

        remove(recycleId);
    }

    /**
     * 判断文件是否过期
     * @return
     */
    public Boolean isOutOfDate(Recycle recycle) {

        // 过期时间
        Date overTime = recycle.getOverTime();
        // 当前时间
        Date currentDate = new Date();
        if (currentDate.getTime() > overTime.getTime()) {
            // 过期
            return true;
        } else {
            // 未过期
            return false;
        }
    }
}
