package group.xuxiake.web.util;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;

/**
 * Author by xuxiake, Date on 2020/3/2 12:23.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Component
public class FastDFSClientWrapper {

    @Resource
    private FastFileStorageClient storageClient;

    /**
     * 上传文件
     * @param file
     * @return
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        StorePath storePath = storageClient.uploadFile(file.getInputStream(),file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()),null);
        return storePath.getFullPath();
    }

    public String uploadFile(InputStream inputStream, long fileSize, String fileExtName) {
        return storageClient.uploadFile(inputStream, fileSize, fileExtName, null).getFullPath();
    }

    public String uploadFile(File file, String fileExtName) throws FileNotFoundException {
        return this.uploadFile(new FileInputStream(file), file.length(), fileExtName);
    }

    /**
     * 下载文件
     * @param filePath 不带IP
     * @return
     */
    public byte[] downloadFile(String filePath) {
        DownloadByteArray downloadByteArray = new DownloadByteArray();
        String groupName = filePath.substring(0, filePath.indexOf("/"));
        String path = filePath.substring(filePath.indexOf("/") + 1, filePath.length());
        byte[] bytes = storageClient.downloadFile(groupName, path, downloadByteArray);
        return bytes;
    }

    /**
     * 得到输入流
     * @param filePath 不带IP
     * @return
     */
    public InputStream getInputStream(String filePath) {
        byte[] bytes = this.downloadFile(filePath);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }
}
