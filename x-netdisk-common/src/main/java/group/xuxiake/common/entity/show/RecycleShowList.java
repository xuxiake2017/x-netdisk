package group.xuxiake.common.entity.show;

import java.util.Date;

/**
 * Author by xuxiake, Date on 2019/1/17.
 * PS: Not easy to write code, please indicate.
 * Description：显示回收站文件列表
 */
public class RecycleShowList {

    // id
    private Integer recycleId;
    // 文件删除时间
    private Date deleteTime;
    // 文件过期时间
    private Date overTime;
    // 文件保存名
    private String fileSaveName;
    // 文件真实名
    private String fileRealName;
    // 文件大小
    private Long fileSize;
    // 文件类型
    private Integer fileType;
    // 是否是文件
    private Integer isDir;

    public Integer getRecycleId() {
        return recycleId;
    }

    public void setRecycleId(Integer recycleId) {
        this.recycleId = recycleId;
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

    public String getFileSaveName() {
        return fileSaveName;
    }

    public void setFileSaveName(String fileSaveName) {
        this.fileSaveName = fileSaveName;
    }

    public String getFileRealName() {
        return fileRealName;
    }

    public void setFileRealName(String fileRealName) {
        this.fileRealName = fileRealName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public Integer getIsDir() {
        return isDir;
    }

    public void setIsDir(Integer isDir) {
        this.isDir = isDir;
    }
}
