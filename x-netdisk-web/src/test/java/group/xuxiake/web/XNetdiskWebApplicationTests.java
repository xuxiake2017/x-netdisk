package group.xuxiake.web;

import group.xuxiake.common.entity.FileMedia;
import group.xuxiake.common.entity.FileOrigin;
import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.mapper.FileMediaMapper;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.FileShareMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.util.RecyclePurgeHelper;
import group.xuxiake.web.util.PasswordEncoder;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XNetdiskWebApplicationTests {

    @Test
    void shouldVerifyNewBcryptPassword() {
        String encoded = PasswordEncoder.encode("correct-password", "alice");

        assertTrue(encoded.startsWith("$2"));
        assertTrue(PasswordEncoder.isPasswordValid("correct-password", encoded, "alice"));
        assertFalse(PasswordEncoder.isPasswordValid("wrong-password", encoded, "alice"));
        assertFalse(PasswordEncoder.needsUpgrade(encoded));
    }

    @Test
    void shouldKeepLegacyPasswordCompatibleUntilLoginUpgrade() {
        String encoded = new SimpleHash("MD5", "legacy-password", "alice", 1024).toString();

        assertTrue(PasswordEncoder.isPasswordValid("legacy-password", encoded, "alice"));
        assertFalse(PasswordEncoder.isPasswordValid("wrong-password", encoded, "alice"));
        assertTrue(PasswordEncoder.needsUpgrade(encoded));
    }

    @Test
    void shouldPurgeOnlyOriginsWithoutOtherFileReferences() {
        UserFileMapper userFileMapper = mock(UserFileMapper.class);
        FileShareMapper fileShareMapper = mock(FileShareMapper.class);
        FileMediaMapper fileMediaMapper = mock(FileMediaMapper.class);
        FileOriginMapper fileOriginMapper = mock(FileOriginMapper.class);
        FileRecycleMapper fileRecycleMapper = mock(FileRecycleMapper.class);

        UserFile exclusiveFile = new UserFile();
        exclusiveFile.setOriginId(10);
        UserFile sharedFile = new UserFile();
        sharedFile.setOriginId(20);
        List<Integer> fileIds = Arrays.asList(1, 2);
        when(userFileMapper.findRecycleChildIds(1)).thenReturn(fileIds);
        when(userFileMapper.findByIds(fileIds)).thenReturn(Arrays.asList(exclusiveFile, sharedFile));
        when(userFileMapper.countByOriginId(10)).thenReturn(0);
        when(userFileMapper.countByOriginId(20)).thenReturn(1);

        FileOrigin origin = new FileOrigin();
        origin.setFilePath("group1/M00/original.dat");
        origin.setPreviewUrl("https://files.example.com/group1/M00/preview.pdf");
        when(fileOriginMapper.selectByPrimaryKey(10)).thenReturn(origin);
        FileMedia media = new FileMedia();
        media.setThumbnailUrl("https://files.example.com/group1/M00/thumb.jpg");
        when(fileMediaMapper.findByOriginId(10)).thenReturn(media);

        List<String> paths = RecyclePurgeHelper.purge(100, 1, userFileMapper, fileShareMapper,
                fileMediaMapper, fileOriginMapper, fileRecycleMapper);

        assertEquals(3, paths.size());
        assertTrue(paths.contains("group1/M00/original.dat"));
        assertTrue(paths.contains("group1/M00/preview.pdf"));
        assertTrue(paths.contains("group1/M00/thumb.jpg"));
        verify(fileOriginMapper).deleteByPrimaryKey(10);
        verify(fileOriginMapper, never()).deleteByPrimaryKey(20);
        verify(userFileMapper).deleteByIds(fileIds);
        verify(fileRecycleMapper).deleteByPrimaryKey(100);
    }

}
