package group.xuxiake.web.service;

import group.xuxiake.common.entity.Page;
import group.xuxiake.common.entity.ShareFile;
import group.xuxiake.common.entity.Result;

public interface ShareFileService {

	/**
	 * 创建分享文件
	 * @param fileSaveName
	 * @return
	 */
  	Result shareFile(String fileSaveName);

	/**
	 * 获取分享文件
	 * @param shareId
	 * @return
	 */
	Result getShareFile(String shareId);

	/**
	 * 根据shareId查找文件
	 * @param shareId
	 * @return
	 */
  	ShareFile findByShareId(String shareId);

	/**
	 * 查找所有分享文件
	 * @param page
	 * @return
	 */
	Result findAll(Page page);

	/**
	 * 增加访问次数
	 * @param id
	 * @return
	 */
  	Integer addAccessTimes(Integer id);

	/**
	 * 增加下载次数
	 * @param id
	 * @return
	 */
	Integer addDownloadTimes(Integer id);

	/**
	 * 增加保存次数
	 * @param id
	 * @return
	 */
  	Integer addSaveTimes(Integer id);

	/**
	 * 删除分享
	 * @param id
	 * @return
	 */
	Result deleteShare(Integer id);

	/**
	 * 检查提取密码
	 * @param shareId 分享文件shareId
	 * @param sharePwd 分享文件密码
	 * @return
	 */
	Result checkPwd(String shareId, String sharePwd);

	/**
	 * 下载分享文件
	 */
    void download(String shareId, String sharePwd, Integer fileId);

	/**
	 * 保存分享文件到网盘
	 * @param shareId
	 * @param sharePwd
	 * @return
	 */
	Result saveToCloud(String shareId, String sharePwd, Integer fileId);

	/**
	 * 查询文件夹子目录
	 * @param shareId
	 * @param sharePwd
	 * @param parentId
	 * @return
	 */
	Result getSubList(String shareId, String sharePwd, Integer parentId);
}
