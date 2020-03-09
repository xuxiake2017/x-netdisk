package group.xuxiake.common.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于记录回收站文件信息的实体类
 * @author xuxiake
 *
 */
public class Recycle implements Serializable {
    private Integer recycleId;

    private Integer fileId;

    private Integer deleteUserId;

    private Date deleteTime;

    private Date overTime;

    private String recycleStatus;
    private FileUpload fileUpload;
    public FileUpload getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(FileUpload fileUpload) {
		this.fileUpload = fileUpload;
	}

	public Integer getRecycleId() {
        return recycleId;
    }

    public void setRecycleId(Integer recycleId) {
        this.recycleId = recycleId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getDeleteUserId() {
        return deleteUserId;
    }

    public void setDeleteUserId(Integer deleteUserId) {
        this.deleteUserId = deleteUserId;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Date getOverTime() {
        return overTime;
    }

    public void setOverTime(Date overTime) {
        this.overTime = overTime;
    }

    public String getRecycleStatus() {
        return recycleStatus;
    }

    public void setRecycleStatus(String recycleStatus) {
        this.recycleStatus = recycleStatus == null ? null : recycleStatus.trim();
    }

	@Override
	public String toString() {
		return "Recycle [recycleId=" + recycleId + ", fileId=" + fileId + ", deleteUserId=" + deleteUserId
				+ ", deleteTime=" + deleteTime + ", overTime=" + overTime + ", recycleStatus=" + recycleStatus
				+ ", fileUpload=" + fileUpload + "]";
	}
    
}