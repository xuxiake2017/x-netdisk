package group.xuxiake.web.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件工具类
 * @author 13155
 *
 */
public class FileUtil {

	/**
	 * 判断文件类型，将数字储存到数据库
	 * @param fileExtName
	 * @return
	 */
	public static Integer getFileType(String fileExtName) {
		switch (fileExtName) {
		// 文档
		case "txt":
			return 1;
		case "doc":
			return 11;
		case "docx":
			return 11;
		case "xls":
			return 12;
		case "xlsx":
			return 12;
		case "ppt":
			return 13;
		case "pdf":
			return 14;
		case "TXT":
			return 1;
		case "DOC":
			return 11;
		case "DOCX":
			return 11;
		case "XLS":
			return 12;
		case "XLSX":
			return 12;
		case "PPT":
			return 13;
		case "PDF":
			return 14;
		// 图片
		case "png":
			return 2;
		case "jpg":
			return 2;
		case "jpeg":
			return 2;
		case "gif":
			return 2;
		case "PNG":
			return 2;
		case "JPG":
			return 2;
		case "JPEG":
			return 2;
		case "GIF":
			return 2;
		// 音频
		case "mp3":
			return 3;
		case "ape":
			return 3;
		case "flac":
			return 3;
		case "MP3":
			return 3;
		case "APE":
			return 3;
		case "FLAC":
			return 3;
		// 视频
		case "mp4":
			return 4;
		case "mkv":
			return 4;
		case "avi":
			return 4;
		case "rmvb":
			return 4;
		case "flv":
			return 4;
		case "MP4":
			return 4;
		case "MKV":
			return 4;
		case "AVI":
			return 4;
		case "RMVB":
			return 4;
		case "FLV":
			return 4;
		// 压缩包
		case "zip":
			return 5;
		case "rar":
			return 5;
		case "7z":
			return 5;
		case "jar":
			return 5;
		case "ZIP":
			return 5;
		case "RAR":
			return 5;
		case "7Z":
			return 5;
		case "JAR":
			return 5;
		//安卓安装包
		case "apk":
			return 6;
		case "APK":
			return 6;
		// 其他
		default:
			return 9;
		}
	}

	/**
	 * 生成临时文件名
	 * @param fileExtName
	 * @return
	 */
	public static String makeFileTempName(String fileExtName) {
		// 为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String formatDate = sdf.format(date);
		return formatDate + "_" + UUID.randomUUID().toString() + "." + fileExtName;
	}

	/**
	 * 生成文件标识符
	 * @return
	 */
	public static String makeFileKey () {
		// 为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String formatDate = sdf.format(date);
		return formatDate + "_" + UUID.randomUUID().toString();
	}
	
	//为防止一个目录下面出现太多文件，要使用hash算法打散存储
		public static String makeCachePath(String savePath, String fileSaveName) {
			// 得到文件名的hashCode的值，得到的就是fileRealName这个字符串对象在内存中的地址
			int hashcode = fileSaveName.hashCode();
			int dir1 = hashcode & 0xf; // 0--15
			int dir2 = (hashcode & 0xf0) >> 4; // 0-15
			// 构造新的保存目录
			String dir = savePath + dir1 + "/" + dir2; // upload\2\3
																// upload\3\5
			// File既可以代表文件也可以代表目录
			File file = new File(dir);
			// 如果目录不存在
			if (!file.exists()) {
				// 创建目录
				file.mkdirs();
			}
			return dir+"/"+fileSaveName;
		}
}
