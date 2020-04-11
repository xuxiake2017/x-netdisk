package group.xuxiake.web.handler;

import group.xuxiake.common.entity.FileMedia;
import group.xuxiake.common.entity.FileOrigin;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.web.util.ConvertVideoUtil;
import group.xuxiake.web.util.FastDFSClientWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Author by xuxiake, Date on 2019/2/27.
 * PS: Not easy to write code, please indicate.
 * Description：开启线程进行视频的转码
 */
@Slf4j
public class VideoTransformHandler implements Runnable {

    private FileOriginMapper fileOriginMapper;
    private String[] paths;
    private FileOrigin fileOrigin;
    private String nginxServer;
    private FastDFSClientWrapper fastDFSClientWrapper;
    private FileMedia fileMedia;

    public VideoTransformHandler(FileOriginMapper fileOriginMapper,
                                 FastDFSClientWrapper fastDFSClientWrapper,
                                 String[] paths,
                                 FileOrigin fileOrigin,
                                 String nginxServer,
                                 FileMedia fileMedia) {
        this.fileOriginMapper = fileOriginMapper;
        this.fastDFSClientWrapper = fastDFSClientWrapper;
        this.paths = paths;
        this.fileOrigin = fileOrigin;
        this.nginxServer = nginxServer;
        this.fileMedia = fileMedia;
    }

    @Override
    public void run() {

        try {
            Long start = System.currentTimeMillis();
            ConvertVideoUtil.convertToMp4(paths, fileMedia);
            Long end = System.currentTimeMillis();
            log.info("视频转码耗时：" + (end - start) / 1000 + "秒");

            String previewUrl = nginxServer + "/" + fastDFSClientWrapper.uploadFile(new File(paths[2]), "mp4");
            fileOrigin.setPreviewUrl(previewUrl);
            fileOriginMapper.updateByPrimaryKeySelective(fileOrigin);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            for (String path : paths) {
                new File(path).delete();
            }
        }
    }
}
