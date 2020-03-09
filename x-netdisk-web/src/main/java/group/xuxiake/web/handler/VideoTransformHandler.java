package group.xuxiake.web.handler;

/**
 * Author by xuxiake, Date on 2019/2/27.
 * PS: Not easy to write code, please indicate.
 * Description：开启线程进行视频的转码
 */

import group.xuxiake.common.entity.FileUpload;
import group.xuxiake.common.mapper.FileUploadMapper;
import group.xuxiake.web.util.ConvertVideoUtil;
import group.xuxiake.web.util.FastDFSClientWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class VideoTransformHandler implements Runnable {

    private FileUploadMapper fileUploadMapper;
    private String[] paths;
    private FileUpload fileUpload;
    private String nginxServer;
    private FastDFSClientWrapper fastDFSClientWrapper;

    public VideoTransformHandler(FileUploadMapper fileUploadMapper, FastDFSClientWrapper fastDFSClientWrapper, String[] paths, FileUpload fileUpload, String nginxServer) {
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
            ConvertVideoUtil.convertToMp4(paths);
            Long end = System.currentTimeMillis();
            log.info("视频转码耗时：" + (end - start) / 1000 + "秒");

            String mediaCachePath = nginxServer + "/" + fastDFSClientWrapper.uploadFile(new File(paths[2]), "mp4");
            fileUpload.setMediaCachePath(mediaCachePath);
            fileUploadMapper.updateFileSelective(fileUpload);
            for (String path : paths) {
                new File(path).delete();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
