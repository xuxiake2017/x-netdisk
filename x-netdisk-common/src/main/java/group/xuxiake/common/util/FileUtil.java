package group.xuxiake.common.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件工具类
 * @author 13155
 *
 */
@Slf4j
public class FileUtil {

	@Data
	static class MimeType {
		private String mimeType;
		private String extension;
	}

	public static Map<String, String> mimeTypes = null;

	static {
		mimeTypes  = new HashMap<>();
		List<MimeType> mimeTypesList = getMimeTypes("mime.types.json");
		if (mimeTypesList != null) {
			mimeTypesList.forEach(mimeType -> {
				mimeTypes.put(mimeType.getExtension(), mimeType.getMimeType());
			});
		}
	}

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

		byte[] convertedBytes = null;
		try {
			String encoding = FileUtil.getTextEncoding(fileBytes);
			if (!encoding.equals("UTF-8")) {
				if ("UTF-8_BOM".equals(encoding)) { // UTF-8带BOM
					convertedBytes = new byte[fileBytes.length - 3];
					System.arraycopy(fileBytes, 3 , convertedBytes, 0, fileBytes.length - 3); // 去掉BOM
				} else {
					// 转换编码
					String payload = IOUtils.toString(fileBytes, encoding);
					try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						IOUtils.write(payload, out, "UTF-8");
						convertedBytes = out.toByteArray();
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			} else { // UTF-8不带BOM
				convertedBytes = fileBytes;
			}

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return convertedBytes;
	}

	public static List<MimeType> getMimeTypes(String filename) {
		ClassPathResource classPathResource = new ClassPathResource(filename);
		Gson gson = new Gson();
		try(InputStream inputStream = classPathResource.getInputStream()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder sb = new StringBuilder();
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				sb.append(readLine);
			}
			Type type = new TypeToken<List<MimeType>>() {
			}.getType();
			return gson.fromJson(sb.toString(), type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getMimeType(String extension) {
		if (mimeTypes == null) {
			return null;
		}
		return mimeTypes.get(extension);
	}
}
