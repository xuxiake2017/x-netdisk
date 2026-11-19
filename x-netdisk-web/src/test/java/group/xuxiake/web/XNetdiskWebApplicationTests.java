package group.xuxiake.web;

import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import group.xuxiake.common.entity.FileMedia;
import group.xuxiake.common.entity.FileOrigin;
import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.entity.param.UserAppRegisteParam;
import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.mapper.FileMediaMapper;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.FileShareMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.util.RecyclePurgeHelper;
import group.xuxiake.common.util.NicknameGenerator;
import group.xuxiake.web.util.ConvertVideoUtil;
import group.xuxiake.web.util.EmailAddressUtil;
import group.xuxiake.web.util.FastDFSClientWrapper;
import group.xuxiake.web.util.PasswordEncoder;
import group.xuxiake.web.aspect.SysLogRecord;
import group.xuxiake.web.service.impl.UserServiceImpl;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XNetdiskWebApplicationTests {

    @Test
    void shouldRecordRegistrationClientTypeForEachEndpoint() throws Exception {
        Method webRegister = UserServiceImpl.class.getMethod("register", UserAppRegisteParam.class);
        Method appRegister = UserServiceImpl.class.getMethod("registerApp", UserAppRegisteParam.class);

        assertEquals(ClientType.WEB, webRegister.getAnnotation(SysLogRecord.class).clientType());
        assertEquals(ClientType.H5, appRegister.getAnnotation(SysLogRecord.class).clientType());
    }

    @Test
    void shouldGenerateFriendlyDefaultNickname() {
        String nickname = NicknameGenerator.generate();

        assertTrue(nickname.matches("(快乐|阳光|可爱|聪明|勇敢|活泼|温柔|开心)"
                + "(小熊|小兔|小鹿|星星|月亮|鲸鱼|海豚|小猫)\\d{4}"));
    }

    @Test
    void shouldNormalizeAndValidateRegistrationEmail() {
        String email = EmailAddressUtil.normalize("  User.Name+tag@Example.COM  ");

        assertEquals("user.name+tag@example.com", email);
        assertTrue(EmailAddressUtil.isValid(email));
        assertFalse(EmailAddressUtil.isValid("invalid-email"));
        assertFalse(EmailAddressUtil.isValid("user@localhost"));
    }

    @Test
    void shouldParseFfprobeVideoInfo() {
        FileMedia fileMedia = new FileMedia();

        ReflectionTestUtils.invokeMethod(ConvertVideoUtil.class, "applyVideoInfo",
                "width=1920\nheight=1080\nduration=12.345000\n", fileMedia);

        assertEquals(1920, fileMedia.getVideoWidth());
        assertEquals(1080, fileMedia.getVideoHeight());
        assertEquals(12345, fileMedia.getVideoDuration());
    }

    @Test
    void shouldConsumeRemainingFastDfsDownloadContentWhenCallbackReturnsEarly() throws Exception {
        FastFileStorageClient storageClient = mock(FastFileStorageClient.class);
        FastDFSClientWrapper wrapper = new FastDFSClientWrapper();
        ReflectionTestUtils.setField(wrapper, "storageClient", storageClient);
        AtomicReference<ByteArrayInputStream> responseStream = new AtomicReference<>();

        when(storageClient.downloadFile(eq("group1"), eq("M00/test.jpg"), any()))
                .thenAnswer(invocation -> {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(
                            "image-content".getBytes(StandardCharsets.UTF_8));
                    responseStream.set(inputStream);
                    DownloadCallback<?> callback = invocation.getArgument(2);
                    return callback.recv(inputStream);
                });

        String result = wrapper.downloadFile("group1/M00/test.jpg", inputStream -> {
            byte[] header = new byte[5];
            assertEquals(5, inputStream.read(header));
            return new String(header, StandardCharsets.UTF_8);
        });

        assertEquals("image", result);
        assertEquals(0, responseStream.get().available());
    }

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
