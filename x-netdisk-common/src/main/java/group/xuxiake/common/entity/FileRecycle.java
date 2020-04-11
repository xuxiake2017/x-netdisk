package group.xuxiake.common.entity;

import java.io.Serializable;
import java.util.Date;

public class FileRecycle implements Serializable {
    private Integer recycleId;

    private Integer fileId;

    private Integer deleteUserId;

    private Date deleteTime;

    private Date overTime;

    private Integer recycleStatus;

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

    public Integer getRecycleStatus() {
        return recycleStatus;
    }

    public void setRecycleStatus(Integer recycleStatus) {
        this.recycleStatus = recycleStatus;
    }
}