package group.xuxiake.web.util;

import group.xuxiake.common.entity.FileMedia;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 用于视音频转码的工具类
 * @author 13155
 *
 */
@Slf4j
public class ConvertVideoUtil {

	private static String FFMPEG_PATH_OF_LINUX = "/usr/bin/./ffmpeg";
	private static String FFPROBE_PATH_OF_LINUX = "/usr/bin/./ffprobe";
	private static String QT_FASTSTART_PATH_OF_LINUX = "/usr/bin/./qt-faststart";
	private static String FFMPEG_PATH_OF_WINDOWS = "D:\\tools\\ffmpeg\\bin\\ffmpeg";
	private static String FFPROBE_PATH_OF_WINDOWS = "D:\\tools\\ffmpeg\\bin\\ffprobe";
	private static String QT_FASTSTART_PATH_OF_WINDOWS = "D:\\tools\\ffmpeg\\bin\\qt-faststart";
	private static final long PROCESS_TIMEOUT_MINUTES = 30;
	static final int PROCESS_OUTPUT_MAX_CHARS = 64 * 1024;

	private static String executeCommand(List<String> command, String operation) {
		Process process = null;
		try {
			process = new ProcessBuilder(command).redirectErrorStream(true).start();
			ProcessOutputCollector outputCollector = new ProcessOutputCollector(process.getInputStream());
			outputCollector.start();
			if (!process.waitFor(PROCESS_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
				process.destroyForcibly();
				outputCollector.join(5000L);
				throw new IllegalStateException(operation + "超时，输出：" + outputCollector.getOutput());
			}
			outputCollector.join();
			if (process.exitValue() != 0) {
				throw new IllegalStateException(operation + "失败，退出码：" + process.exitValue()
						+ "，输出：" + outputCollector.getOutput());
			}
			return outputCollector.getOutput();
		} catch (IOException e) {
			throw new IllegalStateException(operation + "启动失败", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if (process != null) {
				process.destroyForcibly();
			}
			throw new IllegalStateException(operation + "被中断", e);
		}
	}

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

	/**
	 * 使用镜像中安装的 ffprobe 获取视频信息，避免依赖 JAVE 内置的旧版 FFmpeg。
	 */
	public static void readVideoInfo(File videoFile, FileMedia fileMedia) {
		List<String> command = new ArrayList<>();
		command.add(isLinux() ? FFPROBE_PATH_OF_LINUX : FFPROBE_PATH_OF_WINDOWS);
		command.add("-v");
		command.add("error");
		command.add("-select_streams");
		command.add("v:0");
		command.add("-show_entries");
		command.add("stream=width,height:format=duration");
		command.add("-of");
		command.add("default=noprint_wrappers=1");
		command.add(videoFile.getAbsolutePath());

		applyVideoInfo(executeCommand(command, "视频信息读取"), fileMedia);
	}

	static void applyVideoInfo(String output, FileMedia fileMedia) {
		Integer width = null;
		Integer height = null;
		Double durationSeconds = null;
		for (String line : output.split("\\R")) {
			String[] item = line.trim().split("=", 2);
			if (item.length != 2) {
				continue;
			}
			switch (item[0].toLowerCase(Locale.ROOT)) {
			case "width":
				width = Integer.valueOf(item[1]);
				break;
			case "height":
				height = Integer.valueOf(item[1]);
				break;
			case "duration":
				durationSeconds = Double.valueOf(item[1]);
				break;
			default:
				break;
			}
		}
		if (width == null || height == null || durationSeconds == null) {
			throw new IllegalStateException("视频信息不完整：" + output);
		}
		fileMedia.setVideoWidth(width);
		fileMedia.setVideoHeight(height);
		fileMedia.setVideoDuration((int) Math.round(durationSeconds * 1000D));
	}

	// ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
	public static void convertToMp4(String[] paths, FileMedia fileMedia) {

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
		executeCommand(command, "视频转码");
		//mp4格式文件转码后处理(将moov atom元数据移动到视频文件最前面，使视频能边缓存边播放)
		qtFaststart(paths[1], paths[2]);
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
		executeCommand(command, "MP4 faststart处理");
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
		command.add("128k");
		command.add("-f");
		command.add("mp3");
		command.add(paths[1]);
		executeCommand(command, "音频转码");
	}

	/**
	 * 获取视频缩率图
	 * @param videoPath
	 * @param thumbnailPath
	 * @param duration
	 */
	public static void thumbnail(String videoPath, String thumbnailPath, Long duration){

		List<String> command = new ArrayList<>();
		if(isLinux()){
			command.add(FFMPEG_PATH_OF_LINUX);
		}else {
			command.add(FFMPEG_PATH_OF_WINDOWS);
		}
		command.add("-ss");
		if (duration >= 2000L) {
			command.add("2");
		} else {
			command.add("0");
		}
		command.add("-i");
		command.add(videoPath);
		command.add("-f");
		command.add("image2");
		command.add("-y");
		command.add("-frames:v");
		command.add("1");
		command.add(thumbnailPath);

		executeCommand(command, "视频缩略图生成");

	}
}

//判断转码以及mp4格式文件转码后处理是否完成，没完成之前线程会一直被阻塞
class ProcessOutputCollector extends Thread {

	private final java.io.InputStream inputStream;
	private final StringBuilder output = new StringBuilder();

	ProcessOutputCollector(java.io.InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			char[] buffer = new char[1024];
			int length;
			while ((length = reader.read(buffer)) != -1) {
				int remaining = ConvertVideoUtil.PROCESS_OUTPUT_MAX_CHARS - output.length();
				if (remaining > 0) {
					output.append(buffer, 0, Math.min(length, remaining));
				}
			}
		} catch (IOException ignored) {
			// 进程被强制终止时输出流可能关闭，调用方会报告进程超时或退出码。
		}
	}

	String getOutput() {
		return output.toString();
	}
}
