package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.entity.show.FileShowInfo;
import group.xuxiake.common.entity.show.FileShowMedia;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserFileMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserFile record);

    int insertSelective(UserFile record);

    UserFile selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserFile record);

    int updateByPrimaryKey(UserFile record);

    /**
     * 列出父级目录下所有文件夹
     * @param userId
     * @param parentId
     * @return
     */
    List<UserFile> findAllDir(@Param("userId") Integer userId, @Param("parentId") Integer parentId);
    /**
     * 根据文件标识符找文件（每个文件都有唯一的文件标识符）
     * @param key
     * @return
     */
    UserFile findFileByKey(String key);

    /**
     * 根据文件真实名查找文件（用于检测当前目录下是否有同名文件夹）
     * @param file
     * @return
     */
    UserFile findFileByRealName(UserFile file);

    /**
     * 查找所有文件
     * @param userFile
     * @param fileTypes
     * @return
     */
    List<FileShowInfo> findAllFile(@Param("userFile") UserFile userFile, @Param("fileTypes") List<Integer> fileTypes);

    /**
     * 删除文件（假删除）
     * @param key
     * @return
     */
    Integer deleteFile(String key);

    /**
     * 删除文件夹
     * @param id
     */
    void deleteDir(Integer id);

    /**
     * 查询删除文件的文件大小（包括子文件及子文件夹）
     * @param map
     */
    void getSumsizeDel(Map<String, Object> map);

    /**
     * 从回收站还原文件夹
     * @param id
     */
    void rebackDir(Integer id);

    /**
     * 从回收站还原文件时，得到其文件大小（包括子文件及子文件夹）
     * @param map
     */
    void getSumsizeRbk(Map<String, Object> map);


    /**
     * 移动文件
     * @param userFile
     * @return
     */
    Integer moveFile(UserFile userFile);

    /**
     * 根据fileSaveName查找文件（用于文件还原）
     * @param key
     * @return
     */
    UserFile findFileBySaveNameForReback(String key);

    /**
     * 查找所有图片文件（用于相册）
     * @param userId
     * @return
     */
    List<FileShowMedia> findAllImg(Integer userId);

    /**
     * 查询文件路径（文件名）
     * @param parentId
     */
    String findPathname(Integer parentId);

    /**
     * 查找父文件下所有子文件id
     * @param fileId
     * @return
     */
    List<Integer> findChildIds(Integer fileId);

    /**
     * 查询文件路径（id）
     * @param fileId
     * @return
     */
    String findIdPath(Integer fileId);

    /**
     * 根据key更新file
     * @param userFile
     */
    void updateByKeySelective(UserFile userFile);
}