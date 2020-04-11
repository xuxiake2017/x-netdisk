package group.xuxiake.web.util;

import group.xuxiake.common.entity.FileMedia;
import it.sauronsoftware.jave.EncoderException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于视音频转码的工具类
 * @author 13155
 *
 */
@Slf4j
public class ConvertVideoUtil {

	private static String FFMPEG_PATH_OF_LINUX = "/usr/bin/./ffmpeg";
	private static String QT_FASTSTART_PATH_OF_LINUX = "/usr/bin/./qt-faststart";
	private static String FFMPEG_PATH_OF_WINDOWS = "D:\\tools\\ffmpeg\\bin\\ffmpeg";
	private static String QT_FASTSTART_PATH_OF_WINDOWS = "D:\\tools\\ffmpeg\\bin\\qt-faststart";

	//判断当前运行环境
	public static boolean isLinux(){
		String osName = System.getProperty("os.name");
		if(osName.indexOf("Linux") != -1){
			return true;
		}
		return false;
		
	}
	public static boolean checkfile(String path) {
		File file = new File(path);
		if (!file.isFile()) {
			return false;
		}
		return true;
	}

	// ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
	public static void convertToMp4(String[] paths, FileMedia fileMedia) throws EncoderException {

		List<String> command = new ArrayList<String>();
		// 转码命令：ffmpeg -y -i 1.mp4 -vf scale="360:trunc(ow/a/2)*2" -vcodec h264 output.mp4 
		if(isLinux()){
			command.add(FFMPEG_PATH_OF_LINUX);
		}else {
			command.add(FFMPEG_PATH_OF_WINDOWS);
		}
		command.add("-y");
		command.add("-i");
		command.add(paths[0]);

		String vf_ = "";
		if (fileMedia.getVideoWidth() > 960) {
			vf_ = "scale=960:trunc(ow/a/2)*2";
		} else {
			vf_ = "scale=480:trunc(ow/a/2)*2";
		}

		if (!StringUtils.isEmpty(vf_)) {
			command.add("-vf");
			command.add(vf_);
		}

		command.add("-vcodec");
		command.add("h264");
		command.add(paths[1]);
		try {
			//调用线程命令进行转码
			Process videoProcess = new ProcessBuilder(command).start();
			new PrintStream(videoProcess.getErrorStream()).start();
			new PrintStream(videoProcess.getInputStream()).start();
			videoProcess.waitFor();
			//mp4格式文件转码后处理(将moov atom元数据移动到视频文件最前面，使视频能边缓存边播放)
			qtFaststart(paths[1], paths[2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void qtFaststart(String inputPath, String outPath){
		
		List<String> command = new ArrayList<String>();
		//mp4格式文件转码后处理命令：qt-faststart 1.mp4 1_1.mp4
		if(isLinux()){
			command.add(QT_FASTSTART_PATH_OF_LINUX);
		}else {
			command.add(QT_FASTSTART_PATH_OF_WINDOWS);
		}
		command.add(inputPath);
		command.add(outPath);
		Process qtProcess;
		try {
			qtProcess = new ProcessBuilder(command).start();
			new PrintStream(qtProcess.getErrorStream()).start();
			new PrintStream(qtProcess.getInputStream()).start();
			qtProcess.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 降低MP3码率
	 */
	public static void lowKmps(String[] paths){
		
		List<String> command = new ArrayList<String>();
		// 转码命令：ffmpeg -i source.mp3 -vn -ar 44100 -ac 2 -ab 128 -f mp3 output.mp3 
		if(isLinux()){
			command.add(FFMPEG_PATH_OF_LINUX);
		}else {
			command.add(FFMPEG_PATH_OF_WINDOWS);
		}
		command.add("-y");
		command.add("-i");
		command.add(paths[0]);
		command.add("-vn");
		command.add("-ar");
		command.add("44100");
		command.add("-ac");
		command.add("2");
		command.add("-ab");
		command.add("128");
		command.add("-f");
		command.add("mp3");
		command.add(paths[1]);
		try {
			//调用线程命令进行转码
			Process process = new ProcessBuilder(command).start();
			new PrintStream(process.getErrorStream()).start();
			new PrintStream(process.getInputStream()).start();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//判断转码以及mp4格式文件转码后处理是否完成，没完成之前线程会一直被阻塞
class PrintStream extends Thread {

	java.io.InputStream __is = null;

	public PrintStream(java.io.InputStream is) {
		__is = is;
	}

	public void run() {
		try {
			while (this != null) {
				int _ch = __is.read();
				if (_ch != -1) {
					System.out.print((char) _ch);
				}else {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
