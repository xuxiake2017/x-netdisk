package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.Recycle;
import group.xuxiake.common.entity.show.RecycleShowList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RecycleMapper {
    int deleteByPrimaryKey(Integer recycleId);

    int insert(Recycle record);

    int insertSelective(Recycle record);

    Recycle selectByPrimaryKey(Integer recycleId);

    int updateByPrimaryKeySelective(Recycle record);

    int updateByPrimaryKey(Recycle record);

    /**
     * 查出用户回收站所有文件
     * @param deleteUserId
     * @return
     */
    List<RecycleShowList> findByUserId(@Param("deleteUserId") Integer deleteUserId, @Param("fileName") String fileName);

    /**
     * 查出回收站所有文件
     * @return
     */
    List<Recycle> findAll();
}