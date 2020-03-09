package group.xuxiake.web.handler;

import group.xuxiake.common.entity.FileUpload;
import group.xuxiake.common.mapper.FileUploadMapper;
import group.xuxiake.web.util.ConvertVideoUtil;
import group.xuxiake.web.util.FastDFSClientWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Author by xuxiake, Date on 2019/2/27.
 * PS: Not easy to write code, please indicate.
 * Description：开辟线程进行音频的转码
 */
@Slf4j
public class LowKbpsHandler implements Runnable {

    private FileUploadMapper fileUploadMapper;
    private FastDFSClientWrapper fastDFSClientWrapper;
    private String[] paths;
    private FileUpload fileUpload;
    private String nginxServer;

    public LowKbpsHandler(FileUploadMapper fileUploadMapper, FastDFSClientWrapper fastDFSClientWrapper, String[] paths, FileUpload fileUpload, String nginxServer) {
        this.fileUploadMapper = fileUploadMapper;
        this.fastDFSClientWrapper = fastDFSClientWrapper;
        this.paths = paths;
        this.fileUpload = fileUpload;
        this.nginxServer = nginxServer;
    }

    @Override
    public void run() {

        try {
            Long start = System.currentTimeMillis();
            ConvertVideoUtil.lowKmps(paths);
            Long end = System.currentTimeMillis();
            log.info("降低MP3音质耗时：" + (end - start) / 1000 + "秒");
            String mediaCachePath = nginxServer + "/" + fastDFSClientWrapper.uploadFile(new File(paths[1]), "mp3");
            fileUpload.setMediaCachePath(mediaCachePath);
            fileUploadMapper.updateFileSelective(fileUpload);
            for (String path : paths) {
                new File(path).delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
