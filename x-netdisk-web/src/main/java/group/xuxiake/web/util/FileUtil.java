package group.xuxiake.web.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件工具类
 * @author 13155
 *
 */
@Slf4j
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
		case "pptx":
			return 13;
		case "pdf":
			return 14;
		// 图片
		case "png":
			return 2;
		case "jpg":
			return 2;
		case "jpeg":
			return 2;
		// 音频
		case "mp3":
			return 3;
		case "ape":
			return 3;
		case "flac":
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
		// 压缩包
		case "zip":
			return 5;
		case "rar":
			return 5;
		case "7z":
			return 5;
		case "jar":
			return 5;
		//安卓安装包
		case "apk":
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

	/**
	 * 根据输入流返回文件编码
	 * @param is
	 * @return
	 */
	public static String getTextCharset(InputStream is) {
		String code = null;
		try {
			int i = is.read() << 8;
			int p = i + is.read();
			switch (p) {
				case 0xefbb:
					code = "UTF-8";
					break;
				case 0xfffe:
					code = "Unicode";
					break;
				case 0xfeff:
					code = "UTF-16";
					break;
				default:
					code = "GB2312";
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		return code;
	}

	/**
	 * 以指定编码方式写文本文件，存在会覆盖
	 *
	 * @param file
	 *            要写入的文件
	 * @param toCharsetName
	 *            要转换的编码
	 * @param content
	 *            文件内容
	 * @throws Exception
	 */
//	public static void saveFile2Charset(OutputStream outputStream, String toCharsetName,
//										String content) throws Exception {
//		if (!Charset.isSupported(toCharsetName)) {
//			throw new UnsupportedCharsetException(toCharsetName);
//		}
//		OutputStream outputStream = new FileOutputStream(file);
//		//增加头文件标识
//		outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
//		OutputStreamWriter outWrite = new OutputStreamWriter(outputStream,
//				toCharsetName);
//		outWrite.write(content);
//		outWrite.close();
//	}
}
