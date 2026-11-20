package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.StorageDeleteTask;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface StorageDeleteTaskMapper {

    int insertIgnore(String storagePath);

    int deleteByPrimaryKey(Integer id);

    int deleteByPath(String storagePath);

    List<StorageDeleteTask> findDueTasks(@Param("now") Date now, @Param("limit") int limit);

    int claim(@Param("id") Integer id, @Param("now") Date now, @Param("lockUntil") Date lockUntil);

    int markRetry(@Param("id") Integer id,
                  @Param("nextRetryTime") Date nextRetryTime,
                  @Param("lastError") String lastError);
}
