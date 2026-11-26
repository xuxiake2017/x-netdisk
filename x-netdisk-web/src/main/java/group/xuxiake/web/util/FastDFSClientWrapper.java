package group.xuxiake.web.util;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadFileWriter;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    /**
     * 上传文件的同时计算服务端 MD5，避免为了校验客户端摘要而再次读取整个文件。
     */
    public UploadResult uploadFileWithMd5(MultipartFile file) throws IOException {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前运行环境不支持 MD5", e);
        }
        StorePath storePath;
        try (DigestInputStream inputStream = new DigestInputStream(file.getInputStream(), messageDigest)) {
            storePath = storageClient.uploadFile(inputStream, file.getSize(),
                    FilenameUtils.getExtension(file.getOriginalFilename()), null);
        }
        return new UploadResult(storePath.getFullPath(), Hex.encodeHexString(messageDigest.digest()));
    }

    public String uploadFile(InputStream inputStream, long fileSize, String fileExtName) {
        return storageClient.uploadFile(inputStream, fileSize, fileExtName, null).getFullPath();
    }

    public String uploadFile(File file, String fileExtName) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return this.uploadFile(inputStream, file.length(), fileExtName);
        }
    }

    public void deleteFile(String filePath) {
        storageClient.deleteFile(filePath);
    }

	public <T> T downloadFile(String filePath, DownloadCallback<T> callback) {
        String groupName = filePath.substring(0, filePath.indexOf("/"));
        String path = filePath.substring(filePath.indexOf("/") + 1, filePath.length());
		return storageClient.downloadFile(groupName, path, inputStream -> {
			try {
				return callback.recv(inputStream);
			} finally {
				// FastDFS 会复用下载连接。回调提前返回时必须消费剩余响应体，
				// 否则残留文件内容会被下一次请求误解析为协议响应头。
				IOUtils.consume(inputStream);
			}
		});
	}

	public void downloadFile(String filePath, OutputStream outputStream) {
		this.downloadFile(filePath, inputStream -> {
			IOUtils.copyLarge(inputStream, outputStream);
			return null;
		});
	}

	public void downloadFile(String filePath, File targetFile) {
		this.downloadFile(filePath, new DownloadFileWriter(targetFile.getAbsolutePath()));
    }

    public static class UploadResult {
        private final String filePath;
        private final String md5Hex;

        public UploadResult(String filePath, String md5Hex) {
            this.filePath = filePath;
            this.md5Hex = md5Hex;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getMd5Hex() {
            return md5Hex;
        }
    }
}
