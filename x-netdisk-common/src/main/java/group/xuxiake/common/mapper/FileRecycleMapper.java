package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.FileRecycle;
import group.xuxiake.common.entity.show.RecycleShowList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileRecycleMapper {
    int deleteByPrimaryKey(Integer recycleId);

    int insert(FileRecycle record);

    int insertSelective(FileRecycle record);

    FileRecycle selectByPrimaryKey(Integer recycleId);

    int updateByPrimaryKeySelective(FileRecycle record);

    int updateByPrimaryKey(FileRecycle record);

    /**
     * 查出用户回收站所有文件
     * @param deleteUserId
     * @return
     */
    List<RecycleShowList> findByUserId(@Param("deleteUserId") Integer deleteUserId, @Param("fileName") String fileName);
}