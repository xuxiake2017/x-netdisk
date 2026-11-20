package group.xuxiake.web.util;

import group.xuxiake.common.entity.FileOrigin;
import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.util.NetdiskConstant;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件和文件夹下载。文件夹直接以流式 ZIP 输出，不在本地生成临时文件。
 */
@Component
public class FileDownloadUtil {

    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private FileOriginMapper fileOriginMapper;
    @Resource
    private FastDFSClientWrapper fastDFSClientWrapper;

    public void download(UserFile root, HttpServletResponse response) throws IOException {
        if (root == null || !Integer.valueOf(NetdiskConstant.FILE_STATUS_OF_NORMAL).equals(root.getStatus())) {
            throw new IOException("下载文件不存在或状态异常");
        }
        if (Integer.valueOf(NetdiskConstant.FILE_IS_DIR).equals(root.getIsDir())) {
            downloadDirectory(root, response);
        } else {
            downloadFile(root, response);
        }
    }

    private void downloadFile(UserFile userFile, HttpServletResponse response) throws IOException {
        FileOrigin origin = requireOrigin(userFile);
        prepareResponse(response, userFile.getFileName(), "application/octet-stream");
        if (origin.getFileSize() != null) {
            response.setHeader("Content-Length", String.valueOf(origin.getFileSize()));
        }
        try (OutputStream output = response.getOutputStream()) {
            fastDFSClientWrapper.downloadFile(origin.getFilePath(), output);
        }
    }

    private void downloadDirectory(UserFile root, HttpServletResponse response) throws IOException {
        String zipName = safeSegment(root.getFileName()) + ".zip";
        prepareResponse(response, zipName, "application/zip");
        Set<Integer> visited = new HashSet<>();
        Set<String> entries = new HashSet<>();
        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {
            String rootPath = uniqueEntry(safeSegment(root.getFileName()) + "/", entries);
            putDirectoryEntry(zip, rootPath);
            appendDirectory(root, rootPath, zip, visited, entries);
            zip.finish();
        }
    }

    private void appendDirectory(UserFile directory, String directoryPath, ZipOutputStream zip,
                                 Set<Integer> visited, Set<String> entries) throws IOException {
        if (!visited.add(directory.getId())) {
            throw new IOException("检测到循环文件夹结构");
        }
        List<UserFile> children = userFileMapper.findChildren(directory.getUserId(), directory.getId());
        for (UserFile child : children) {
            String childPath = uniqueEntry(directoryPath + safeSegment(child.getFileName())
                    + (Integer.valueOf(NetdiskConstant.FILE_IS_DIR).equals(child.getIsDir()) ? "/" : ""), entries);
            if (Integer.valueOf(NetdiskConstant.FILE_IS_DIR).equals(child.getIsDir())) {
                putDirectoryEntry(zip, childPath);
                appendDirectory(child, childPath, zip, visited, entries);
            } else {
                FileOrigin origin = requireOrigin(child);
                zip.putNextEntry(new ZipEntry(childPath));
                fastDFSClientWrapper.downloadFile(origin.getFilePath(), zip);
                zip.closeEntry();
            }
        }
    }

    private FileOrigin requireOrigin(UserFile userFile) throws IOException {
        FileOrigin origin = fileOriginMapper.findByUserFileId(userFile.getId());
        if (origin == null || origin.getFilePath() == null) {
            throw new IOException("文件存储信息不存在: " + userFile.getId());
        }
        return origin;
    }

    private void putDirectoryEntry(ZipOutputStream zip, String path) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.closeEntry();
    }

    private String uniqueEntry(String path, Set<String> entries) {
        String candidate = path;
        boolean directory = path.endsWith("/");
        String value = directory ? path.substring(0, path.length() - 1) : path;
        int slash = value.lastIndexOf('/');
        String parent = slash >= 0 ? value.substring(0, slash + 1) : "";
        String name = slash >= 0 ? value.substring(slash + 1) : value;
        int dot = directory ? -1 : name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String extension = dot > 0 ? name.substring(dot) : "";
        int suffix = 1;
        while (!entries.add(candidate)) {
            candidate = parent + base + "(" + suffix++ + ")" + extension + (directory ? "/" : "");
        }
        return candidate;
    }

    private String safeSegment(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "未命名";
        }
        StringBuilder safe = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            safe.append(c == '/' || c == '\\' || Character.isISOControl(c) ? '_' : c);
        }
        String value = safe.toString().trim();
        return value.isEmpty() || ".".equals(value) || "..".equals(value) ? "未命名" : value;
    }

    private void prepareResponse(HttpServletResponse response, String fileName, String contentType)
            throws IOException {
        response.reset();
        response.setContentType(contentType);
        String encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"download\"; filename*=UTF-8''" + encoded);
    }
}
