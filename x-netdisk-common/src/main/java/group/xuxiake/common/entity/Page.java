package group.xuxiake.common.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 用于分页的实体类
 * @author xuxiake
 *
 */
public class Page implements Serializable {

	private Integer pageNum;
	private Integer pages;
	private Integer pageSize;
	private Integer total;
	private Integer startLine;
	private String url;
	private List list;
	private Integer parentId;
	private String fileRealName;
	private String orderBy; // 排序方式 fileName: 文件名 uploadTime: 上传时间
	public Page() {
		this.pageNum = 1;
		this.pageSize = 15;
		this.parentId = -1;
		this.orderBy = "fileName";
	}
	public Integer getPageNum() {
		return pageNum;
	}
	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}
	public Integer getPages() {
		return pages;
	}
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public Integer getStartLine() {
		return startLine;
	}
	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List getList() {
		return list;
	}
	public void setList(List list) {
		this.list = list;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	public String getFileRealName() {
		return fileRealName;
	}
	public void setFileRealName(String fileRealName) {
		this.fileRealName = fileRealName;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
}
