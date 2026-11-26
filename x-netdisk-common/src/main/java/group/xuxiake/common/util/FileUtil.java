package group.xuxiake.common.util;

import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
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
		if (fileExtName == null) {
			return NetdiskConstant.FILE_TYPE_OF_OTHER;
		}
		switch (fileExtName.toLowerCase(Locale.ROOT)) {
		// 文档
		case "txt":
			return NetdiskConstant.FILE_TYPE_OF_TXT;
		case "doc":
		case "docx":
		case "odt":
		case "rtf":
			return NetdiskConstant.FILE_TYPE_OF_WORD;
		case "xls":
		case "xlsx":
		case "ods":
		case "csv":
			return NetdiskConstant.FILE_TYPE_OF_EXCEL;
		case "ppt":
		case "pptx":
		case "odp":
			return NetdiskConstant.FILE_TYPE_OF_POWERPOINT;
		case "pdf":
			return NetdiskConstant.FILE_TYPE_OF_PDF;
		// 图片
		case "png":
		case "jpg":
		case "jpeg":
		case "gif":
		case "bmp":
			return NetdiskConstant.FILE_TYPE_OF_PIC;
		// 音频
		case "mp3":
		case "ape":
		case "flac":
		case "wav":
		case "aac":
		case "m4a":
		case "ogg":
		case "opus":
			return NetdiskConstant.FILE_TYPE_OF_MUSIC;
		// 视频
		case "mp4":
		case "mkv":
		case "avi":
		case "flv":
		case "mov":
		case "webm":
		case "wmv":
		case "mpg":
		case "mpeg":
		case "m4v":
		case "3gp":
			return NetdiskConstant.FILE_TYPE_OF_VIDEO;
		// 压缩包
		case "zip":
		case "rar":
		case "7z":
		case "jar":
			return NetdiskConstant.FILE_TYPE_OF_ZIP;
		//安卓安装包
		case "apk":
			return NetdiskConstant.FILE_TYPE_OF_APK;
		// 其他
		default:
			return NetdiskConstant.FILE_TYPE_OF_OTHER;
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
	 * 根据txt文件bytes获取编码
	 * @param fileBytes
	 * @return
	 */
	public static String getTextEncoding(byte[] fileBytes) {
		byte[] bytes = Arrays.copyOf(fileBytes, 3);
		String encoding = "gb2312";
		if (bytes[0] == -1 && bytes[1] == -2 ) {
			encoding = "UTF-16";
		} else if (bytes[0] == -2 && bytes[1] == -1 ) {
			encoding = "Unicode";
		} else if(bytes[0]==-17 && bytes[1]==-69 && bytes[2] ==-65) {
			encoding = "UTF-8_BOM";
		} else {
			encoding = guessEncoding(fileBytes);
		}
		return encoding;
	}

	/**
     * 根据字节判断编码
	 * @param bytes
     * @return
     */
	public static String guessEncoding(byte[] bytes) {
		String DEFAULT_ENCODING = "UTF-8";
		UniversalDetector detector =
				new UniversalDetector(null);
		detector.handleData(bytes, 0, bytes.length);
		detector.dataEnd();
		String encoding = detector.getDetectedCharset();
		detector.reset();
		if (encoding == null) {
			encoding = DEFAULT_ENCODING;
		}
		return encoding;
	}

	/**
     *
	 * @param fileBytes
     * @return
     */
	public static byte[] textEncodingConvert(byte[] fileBytes) {

		String encoding = FileUtil.getTextEncoding(fileBytes);
		if ("UTF-8".equals(encoding)) { // UTF-8不带BOM
			return fileBytes;
		}
		if ("UTF-8_BOM".equals(encoding)) { // UTF-8带BOM
			byte[] convertedBytes = new byte[fileBytes.length - 3];
			System.arraycopy(fileBytes, 3, convertedBytes, 0, convertedBytes.length);
			return convertedBytes;
		}

		String payload = new String(fileBytes, Charset.forName(encoding));
		return payload.getBytes(StandardCharsets.UTF_8);
	}
}
