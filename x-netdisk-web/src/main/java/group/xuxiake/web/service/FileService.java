package group.xuxiake.web.service;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.entity.param.FileUploadParamByMD5;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

	/**
	 * 检查文件MD5值是否在数据库存在
	 * @param md5Hex
	 * @return
	 */
	Result checkMd5(String md5Hex);

	/**
	 * 文件MD5值在数据库存在，假上传
	 * @param param
	 * @return
	 */
	Result uploadMD5(FileUploadParamByMD5 param);

	/**
	 * 列出所有文件夹，用于文件移动
	 * @param parentId
	 * @return
	 */
	Result listAllDir(Integer parentId);

	/**
	 * 文件重命名
	 * @param userFile
	 * @return
	 */
	Result reName(UserFile userFile);

	/**
	 * 文件上传
	 * @param parentId
	 * @param md5Hex
	 * @param lastModifiedDate
	 * @param fileRealName
	 * @param file
	 * @return
	 */
	Result fileUpload(Integer parentId, String md5Hex, Long lastModifiedDate, String fileRealName, MultipartFile file);

	/**
	 * 创建文件夹
	 * @param userFile
	 * @return
	 */
  	Result mkDir(UserFile userFile);

	/**
	 * 删除文件（假删除）
	 * @param fileKey
	 * @return
	 */
	Result deleteFile(String fileKey);

	/**
	 * 移动文件
	 * @param param
	 * @return
	 */
	Result moveFile(UserFile param);

	/**
	 * 文件下载
	 * @param fileKey
	 * @param fileId
	 */
	void downLoad(String fileKey, Integer fileId);

	/**
	 * 查找文件路径
	 * @param parentId
	 */
	Result findPathname(Integer parentId);

	/**
	 * 查找父文件下所有子文件
	 * @param fileId
	 * @return
	 */
	Result findChildIds(Integer fileId);

	/**
	 * 前端页面搜索后，点击文件夹跳转，得到储存的文件路径
	 * @param fileId
	 * @return
	 */
    Result getPathStore(Integer fileId);

	/**
	 * findById
	 * @param id
	 * @return
	 */
	Result findById(Integer id);

	/**
	 * 获取文件媒体信息
	 * @param fileKey
	 * @return
	 */
	Result getFileMediaInfo(String fileKey);
}
