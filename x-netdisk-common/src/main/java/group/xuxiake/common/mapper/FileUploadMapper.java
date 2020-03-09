package group.xuxiake.common.mapper;

import group.xuxiake.common.entity.FileUpload;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FileUploadMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(FileUpload record);

	int insertSelective(FileUpload record);

	FileUpload selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(FileUpload record);

	int updateByPrimaryKey(FileUpload record);

	/**
	 * 根据文件保存名找文件（每个文件都有唯一的文件保存名，作为标识符）
	 * @param fileSaveName
	 * @return
	 */
	FileUpload findFileBySaveName(String fileSaveName);

	/**
	 * 根据md5值查找文件（用于秒传）
	 * @param md5Hex
	 * @return
	 */
	List<FileUpload> findFileByMd5Hex(String md5Hex);

	/**
	 * 根据文件真实名查找文件（用于检测当前目录下是否有同名文件夹）
	 * @param fileUpload
	 * @return
	 */
	List<FileUpload> findFileByRealName(FileUpload fileUpload);

	/**
	 * 查找所有文件
	 * @param fileUpload
	 * @param fileTypes
	 * @return
	 */
	List<FileUpload> findAllFile(@Param("fileUpload") FileUpload fileUpload, @Param("fileTypes") List<Integer> fileTypes);

	/**
	 * 删除文件（假删除）
	 * @param fileSaveName
	 * @return
	 */
	Integer deleteFile(String fileSaveName);

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
	 * 选择性的更新文件（以fileSaveName为key）
	 * @param fileUpload
	 * @return
	 */
	Integer updateFileSelective(FileUpload fileUpload);

	/**
	 * 找到当前用户、当前父级目录下所有文件夹
	 * @param fileUpload
	 * @return
	 */
	List<FileUpload> findAllDir(FileUpload fileUpload);

	/**
	 * 移动文件
	 * @param fileUpload
	 * @return
	 */
	Integer moveFile(FileUpload fileUpload);

	/**
	 * 根据fileSaveName查找文件（用于文件还原）
	 * @param fileSaveName
	 * @return
	 */
	FileUpload findFileBySaveNameForReback(String fileSaveName);

	/**
	 * 查找所有图片文件（用于相册）
	 * @param userId
	 * @return
	 */
	List<FileUpload> findAllImg(Integer userId);

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
}