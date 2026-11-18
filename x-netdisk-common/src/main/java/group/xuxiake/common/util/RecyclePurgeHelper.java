package group.xuxiake.common.util;

import group.xuxiake.common.entity.FileOrigin;
import group.xuxiake.common.entity.FileMedia;
import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.mapper.FileMediaMapper;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.FileShareMapper;
import group.xuxiake.common.mapper.UserFileMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.net.URI;

/**
 * 清理回收站中的用户文件引用，并返回已经没有数据库引用的物理文件路径。
 */
public final class RecyclePurgeHelper {

    private RecyclePurgeHelper() {
    }

    public static List<String> purge(Integer recycleId, Integer rootFileId,
                                     UserFileMapper userFileMapper,
                                     FileShareMapper fileShareMapper,
                                     FileMediaMapper fileMediaMapper,
                                     FileOriginMapper fileOriginMapper,
                                     FileRecycleMapper fileRecycleMapper) {
        List<Integer> fileIds = userFileMapper.findRecycleChildIds(rootFileId);
        if (fileIds == null || fileIds.isEmpty()) {
            fileRecycleMapper.deleteByPrimaryKey(recycleId);
            return new ArrayList<>();
        }

        List<UserFile> files = userFileMapper.findByIds(fileIds);
        Set<Integer> originIds = new HashSet<>();
        for (UserFile file : files) {
            if (file.getOriginId() != null) {
                originIds.add(file.getOriginId());
            }
        }

        fileShareMapper.deleteByFileIds(fileIds);
        userFileMapper.deleteByIds(fileIds);
        fileRecycleMapper.deleteByPrimaryKey(recycleId);

        Set<String> storagePaths = new HashSet<>();
        for (Integer originId : originIds) {
            if (userFileMapper.countByOriginId(originId) == 0) {
                FileOrigin origin = fileOriginMapper.selectByPrimaryKey(originId);
                FileMedia media = fileMediaMapper.findByOriginId(originId);
                if (origin != null) {
                    addStoragePath(storagePaths, origin.getFilePath());
                    addStoragePath(storagePaths, origin.getPreviewUrl());
                }
                if (media != null) {
                    addStoragePath(storagePaths, media.getThumbnailUrl());
                    addStoragePath(storagePaths, media.getMusicPoster());
                }
                fileMediaMapper.deleteByOriginId(originId);
                fileOriginMapper.deleteByPrimaryKey(originId);
            }
        }
        return new ArrayList<>(storagePaths);
    }

    private static void addStoragePath(Set<String> storagePaths, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        String path = value.trim();
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                path = URI.create(path).getPath();
            }
        } catch (IllegalArgumentException ignored) {
            return;
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.indexOf('/') > 0) {
            storagePaths.add(path);
        }
    }
}
