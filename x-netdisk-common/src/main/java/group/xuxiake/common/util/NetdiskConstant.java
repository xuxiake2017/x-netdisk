package group.xuxiake.common.util;

public class NetdiskConstant {

    // 配置properties文件的名称
    public static final String CONFIG_PROPERTIES_FILE_NAME = "config.properties";

    // 数据库数据统一状态-正常
    public static final int DATA_NORMAL_STATUS = 0;
    // 数据库数据统一状态-删除
    public static final int DATA_DELETE_STATUS = 1;

    // 文件类型-文件夹
    public static final int FILE_TYPE_OF_DIR = 0;
    // 文件类型-文本
    public static final int FILE_TYPE_OF_TXT = 1;
    // 文件类型-word
    public static final int FILE_TYPE_OF_WORD = 11;
    // 文件类型-excel
    public static final int FILE_TYPE_OF_EXCEL = 12;
    // 文件类型-PowerPoint
    public static final int FILE_TYPE_OF_POWERPOINT = 13;
    // 文件类型-PowerPoint
    public static final int FILE_TYPE_OF_PDF = 14;
    // 文件类型-图片
    public static final int FILE_TYPE_OF_PIC = 2;
    // 文件类型-音乐
    public static final int FILE_TYPE_OF_MUSIC = 3;
    // 文件类型-视频
    public static final int FILE_TYPE_OF_VIDEO = 4;
    // 文件类型-压缩文件
    public static final int FILE_TYPE_OF_ZIP = 5;
    // 文件类型-安卓安装包
    public static final int FILE_TYPE_OF_APK = 6;
    // 文件类型-其他
    public static final int FILE_TYPE_OF_OTHER = 9;

    // 文件是否是文件夹-是
    public static final int FILE_IS_DIR = 0;
    // 文件是否是文件夹-不是
    public static final int FILE_IS_NOT_DIR = 1;

    // 用户状态-普通用户
    public static final int USER_STATUS_NORMAL = 0;
    // 用户状态-不存在（已删除）
    public static final int USER_STATUS_NOT_EXIST = 1;
    // 用户状态-VIP用户
    public static final int USER_STATUS_VIP = 2;
    // 用户状态-被管理员删除
    public static final int USER_STATUS_DEL_BY_ADMIN = 3;
    // 用户状态-未激活
    public static final int USER_STATUS_NOT_VERIFY = 9;

    // 消息类型-success
    public static final int MESSAGE_TYPE_OF_SUCCESS = 0;
    // 消息类型-info
    public static final int MESSAGE_TYPE_OF_INFO = 1;
    // 消息类型-warning
    public static final int MESSAGE_TYPE_OF_WARNING = 2;
    // 消息类型-error
    public static final int MESSAGE_TYPE_OF_ERROR = 3;

    // 文件状态-正常
    public static final int FILE_STATUS_OF_NORMAL = 0;
    // 文件状态-删除
    public static final int FILE_STATUS_OF_DEL = 1;
    // 文件状态-因父文件夹删除被动删除
    public static final int FILE_STATUS_OF_DEL_PASSIVE = 2;
    // 文件状态-被管理员删除
    public static final int FILE_STATUS_OF_DEL_BY_ADMIN = 3;

    // 回收站文件状态-文件被删除，还可以找回
    public static final int RECYCLE_STATUS_FILE_DEL = 0;
    // 回收站文件状态-文件被删除，已经找回
    public static final int RECYCLE_STATUS_FILE_HAVE_BEEN_RESTORE = 1;
    // 回收站文件状态-文件被彻底删除，不能找回
    public static final int RECYCLE_STATUS_FILE_HAVE_BEEN_DEL_FOREVER = 2;

    // 分享状态-正常
    public static final int SHARE_STATUS_OF_NORMAL = 0;
    // 分享状态-删除
    public static final int SHARE_STATUS_OF_DEL = 1;
    // 分享状态-被管理员删除
    public static final int SHARE_STATUS_OF_DEL_BY_ADMIN = 2;

    // 好友申请操作-同意
    public static final int FRIEND_APPLY_FOR_OPTION_OF_AGREE = 1;
    // 好友申请操作-拒绝
    public static final int FRIEND_APPLY_FOR_OPTION_OF_REFUSE = 2;
}
