package group.xuxiake.web.service;

import group.xuxiake.common.entity.FileUpload;
import group.xuxiake.common.entity.Result;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

	/**
	 * 检查文件MD5值是否在数据库存在
	 * @param md5Hex
	 * @return
	 */
	Result checkMd5(String md5Hex);

	/**
	 * 文件MD5值在数据库存在，假上传
	 * @param fileUpload
	 * @return
	 */
	Result uploadMD5(FileUpload fileUpload);

	/**
	 * 列出所有文件夹，用于文件移动
	 * @param parentId
	 * @return
	 */
	Result listAllDir(Integer parentId);

	/**
	 * 文件重命名
	 * @param fileUpload
	 * @return
	 */
	Result reName(FileUpload fileUpload);

	/**
	 * 文件上传
	 * @param parentId
	 * @param md5Hex
	 * @param lastModifiedDate
	 * @param file
	 * @return
	 */
	Result fileUpload(Integer parentId, String md5Hex, Long lastModifiedDate, MultipartFile file);

	/**
	 * 创建文件夹
	 * @param fileUpload
	 * @return
	 */
  	Result mkDir(FileUpload fileUpload);
  	//删除文件（假删除）
  	Result deleteFile(String fileSaveName);

	/**
	 * 移动文件
	 * @param fileUpload
	 * @return
	 */
	Result moveFile(FileUpload fileUpload);

	/**
	 * 文件下载
	 * @param fileUpload
	 */
	void downLoad(FileUpload fileUpload);

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
}
