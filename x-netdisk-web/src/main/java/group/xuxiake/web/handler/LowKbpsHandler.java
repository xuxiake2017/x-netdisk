package group.xuxiake.web.handler;

import group.xuxiake.common.entity.FileOrigin;
import group.xuxiake.common.mapper.FileOriginMapper;
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

    private FileOriginMapper fileOriginMapper;
    private FastDFSClientWrapper fastDFSClientWrapper;
    private String[] paths;
    private FileOrigin fileOrigin;
    private String nginxServer;

    public LowKbpsHandler(FileOriginMapper fileOriginMapper, FastDFSClientWrapper fastDFSClientWrapper, String[] paths, FileOrigin fileOrigin, String nginxServer) {
        this.fileOriginMapper = fileOriginMapper;
        this.fastDFSClientWrapper = fastDFSClientWrapper;
        this.paths = paths;
        this.fileOrigin = fileOrigin;
        this.nginxServer = nginxServer;
    }

    @Override
    public void run() {

        try {
            Long start = System.currentTimeMillis();
            ConvertVideoUtil.lowKmps(paths);
            Long end = System.currentTimeMillis();
            log.info("降低MP3音质耗时：" + (end - start) / 1000 + "秒");
            String previewUrl = nginxServer + "/" + fastDFSClientWrapper.uploadFile(new File(paths[1]), "mp3");
            fileOrigin.setPreviewUrl(previewUrl);
            fileOriginMapper.updateByPrimaryKeySelective(fileOrigin);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (String path : paths) {
                new File(path).delete();
            }
        }
    }
}
