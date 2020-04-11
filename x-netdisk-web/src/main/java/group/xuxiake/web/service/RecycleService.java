package group.xuxiake.web.service;

import group.xuxiake.common.entity.Page;
import group.xuxiake.common.entity.Result;

public interface RecycleService {

	/**
	 * 查询回收站列表
	 * @param page
	 * @return
	 */
	Result toRecycleList(Page page);

	/**
	 * 删除回收站文件
	 * @param id
	 * @return
	 */
	Result delete(Integer id);

	/**
	 * 恢复文件
	 * @param recycleId
	 * @param fileKey
	 * @return
	 */
	Result reback(Integer recycleId, String fileKey);
	
}
