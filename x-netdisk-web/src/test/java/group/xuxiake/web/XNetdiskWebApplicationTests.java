package group.xuxiake.web;

import com.github.tobato.fastdfs.domain.proto.storage.DownloadCallback;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import group.xuxiake.common.entity.FileMedia;
import group.xuxiake.common.entity.FileOrigin;
import group.xuxiake.common.entity.FileShare;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.UserFile;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.FileRecycle;
import group.xuxiake.common.entity.param.UserAppRegisteParam;
import group.xuxiake.common.entity.param.UserProfileUpdateParam;
import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.mapper.FileMediaMapper;
import group.xuxiake.common.mapper.FileOriginMapper;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.FileShareMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.common.mapper.StorageDeleteTaskMapper;
import group.xuxiake.common.util.RecyclePurgeHelper;
import group.xuxiake.common.util.NicknameGenerator;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.configuration.CustomConfiguration;
import group.xuxiake.web.util.ConvertVideoUtil;
import group.xuxiake.web.util.EmailAddressUtil;
import group.xuxiake.web.util.FastDFSClientWrapper;
import group.xuxiake.web.util.FileDownloadUtil;
import group.xuxiake.web.util.PasswordEncoder;
import group.xuxiake.web.aspect.SysLogRecord;
import group.xuxiake.web.service.impl.UserServiceImpl;
import group.xuxiake.web.service.impl.ShareFileServiceImpl;
import group.xuxiake.web.service.impl.RecycleServiceImpl;
import group.xuxiake.web.service.impl.StorageRollbackCleanupService;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XNetdiskWebApplicationTests {

    @Test
    void shouldCalculateMd5WhileUploadingToStorage() throws Exception {
        byte[] content = "netdisk-content".getBytes(StandardCharsets.UTF_8);
        FastFileStorageClient storageClient = mock(FastFileStorageClient.class);
        when(storageClient.uploadFile(any(InputStream.class), eq((long) content.length), eq("txt"), eq(null)))
                .thenAnswer(invocation -> {
                    InputStream inputStream = invocation.getArgument(0);
                    byte[] uploaded = new byte[content.length];
                    int offset = 0;
                    while (offset < uploaded.length) {
                        int count = inputStream.read(uploaded, offset, uploaded.length - offset);
                        if (count < 0) {
                            break;
                        }
                        offset += count;
                    }
                    assertTrue(Arrays.equals(content, uploaded));
                    return new StorePath("group1", "M00/test.txt");
                });
        FastDFSClientWrapper wrapper = new FastDFSClientWrapper();
        ReflectionTestUtils.setField(wrapper, "storageClient", storageClient);
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content);

        FastDFSClientWrapper.UploadResult uploadResult = wrapper.uploadFileWithMd5(file);

        assertEquals("group1/M00/test.txt", uploadResult.getFilePath());
        assertEquals("3adf6a71fee2da218e8ef1fffb16acb4", uploadResult.getMd5Hex());
    }

    @Test
    void shouldQueueStorageDeleteWhenRollbackCleanupFails() {
        FastDFSClientWrapper fastDFSClientWrapper = mock(FastDFSClientWrapper.class);
        StorageDeleteTaskMapper storageDeleteTaskMapper = mock(StorageDeleteTaskMapper.class);
        StorageRollbackCleanupService service = new StorageRollbackCleanupService();
        ReflectionTestUtils.setField(service, "fastDFSClientWrapper", fastDFSClientWrapper);
        ReflectionTestUtils.setField(service, "storageDeleteTaskMapper", storageDeleteTaskMapper);
        doThrow(new RuntimeException("storage unavailable"))
                .when(fastDFSClientWrapper).deleteFile("group1/M00/orphan.dat");

        service.deleteOrQueue("group1/M00/orphan.dat");

        verify(storageDeleteTaskMapper).insertIgnore("group1/M00/orphan.dat");
    }

    @Test
    void shouldValidateProfileFieldsAndPasswordStrength() {
        UserServiceImpl service = new UserServiceImpl();
        UserProfileUpdateParam valid = new UserProfileUpdateParam();
        valid.setNickName("快乐小熊0001");
        valid.setSex(1);
        assertTrue(((Boolean) ReflectionTestUtils.invokeMethod(service, "isValidProfile", valid)).booleanValue());

        UserProfileUpdateParam invalid = new UserProfileUpdateParam();
        invalid.setNickName("   ");
        assertFalse(((Boolean) ReflectionTestUtils.invokeMethod(service, "isValidProfile", invalid)).booleanValue());
        assertTrue(((Boolean) ReflectionTestUtils.invokeMethod(service, "isStrongPassword", "netdisk2026")).booleanValue());
        assertFalse(((Boolean) ReflectionTestUtils.invokeMethod(service, "isStrongPassword", "12345678")).booleanValue());
    }

    @Test
    void shouldRestoreToResourcesWhenOriginalParentWasPurged() {
        FileRecycleMapper recycleMapper = mock(FileRecycleMapper.class);
        UserFileMapper userFileMapper = mock(UserFileMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        FileOriginMapper fileOriginMapper = mock(FileOriginMapper.class);
        RecycleServiceImpl service = new RecycleServiceImpl();
        ReflectionTestUtils.setField(service, "recycleMapper", recycleMapper);
        ReflectionTestUtils.setField(service, "userFileMapper", userFileMapper);
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        ReflectionTestUtils.setField(service, "fileOriginMapper", fileOriginMapper);

        User principal = new User();
        principal.setId(10);
        principal.setUsedMemory(0L);
        principal.setTotalMemory(1024L);
        Subject subject = mock(Subject.class);
        when(subject.getPrincipal()).thenReturn(principal);

        UserFile recycledFile = new UserFile();
        recycledFile.setId(1);
        recycledFile.setKey("recycled-key");
        recycledFile.setUserId(10);
        recycledFile.setParentId(999);
        recycledFile.setIsDir(1);
        recycledFile.setStatus(1);
        FileRecycle recycle = new FileRecycle();
        recycle.setRecycleId(20);
        recycle.setFileId(1);
        recycle.setDeleteUserId(10);
        recycle.setRecycleStatus(0);
        recycle.setOverTime(new Date(System.currentTimeMillis() + 60000));
        UserFile resources = new UserFile();
        resources.setId(88);
        FileOrigin origin = new FileOrigin();
        origin.setFileSize(10L);

        when(userFileMapper.findFileBySaveNameForReback("recycled-key")).thenReturn(recycledFile);
        when(recycleMapper.selectByPrimaryKeyForUpdate(20)).thenReturn(recycle);
        when(userMapper.selectByPrimaryKeyForUpdate(10)).thenReturn(principal);
        when(userFileMapper.sumNormalFileSizeByUserId(10)).thenReturn(0L, 10L);
        when(userFileMapper.selectByPrimaryKey(999)).thenReturn(null);
        when(userFileMapper.findFileByRealName(any(UserFile.class))).thenReturn(resources);
        when(fileOriginMapper.findByUserFileId(1)).thenReturn(origin);

        ThreadContext.bind(subject);
        TransactionSynchronizationManager.initSynchronization();
        try {
            Result result = service.reback(20, "recycled-key");

            assertEquals(NetdiskErrMsgConstant.REQUEST_SUCCESS, result.getCode());
            assertEquals(88, recycledFile.getParentId());
            assertEquals(10L, principal.getUsedMemory());
            verify(recycleMapper).deleteByPrimaryKey(20);
            verify(userFileMapper).updateByKeySelective(recycledFile);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            ThreadContext.unbindSubject();
        }
    }

    @Test
    void shouldLockSharePasswordAfterTooManyFailures() {
        FileShareMapper shareMapper = mock(FileShareMapper.class);
        UserFileMapper userFileMapper = mock(UserFileMapper.class);
        RedisUtils redisUtils = mock(RedisUtils.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        CustomConfiguration custom = new CustomConfiguration();
        custom.setShareMaxPasswordAttempts(2);
        custom.setSharePasswordLockTime(600000L);
        AppConfiguration appConfiguration = new AppConfiguration();
        appConfiguration.setCustomConfiguration(custom);
        ShareFileServiceImpl service = new ShareFileServiceImpl();
        ReflectionTestUtils.setField(service, "fileShareMapper", shareMapper);
        ReflectionTestUtils.setField(service, "userFileMapper", userFileMapper);
        ReflectionTestUtils.setField(service, "redisUtils", redisUtils);
        ReflectionTestUtils.setField(service, "request", request);
        ReflectionTestUtils.setField(service, "appConfiguration", appConfiguration);

        FileShare share = new FileShare();
        share.setShareId("private");
        share.setSharePwd("ABCD");
        share.setShareUserId(10);
        share.setFileId(1);
        share.setIsPublic(0);
        UserFile root = new UserFile();
        root.setId(1);
        root.setUserId(10);
        root.setStatus(0);
        when(shareMapper.findByShareId("private")).thenReturn(share);
        when(userFileMapper.selectByPrimaryKey(1)).thenReturn(root);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(redisUtils.incr(anyString(), eq(1L))).thenReturn(1L, 2L);

        Result first = service.checkPwd("private", "wrong");
        Result second = service.checkPwd("private", "wrong");

        assertEquals(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG, first.getCode());
        assertEquals(NetdiskErrMsgConstant.SHARE_PWD_ATTEMPTS_EXCEEDED, second.getCode());
        verify(redisUtils).expire(anyString(), eq(600L));
    }

    @Test
    void shouldRejectExpiredAndRecycledSharesAndAllowPublicShare() {
        FileShareMapper shareMapper = mock(FileShareMapper.class);
        UserFileMapper userFileMapper = mock(UserFileMapper.class);
        ShareFileServiceImpl service = new ShareFileServiceImpl();
        ReflectionTestUtils.setField(service, "fileShareMapper", shareMapper);
        ReflectionTestUtils.setField(service, "userFileMapper", userFileMapper);

        FileShare expired = new FileShare();
        expired.setFileId(1);
        expired.setShareUserId(10);
        expired.setOverTime(new Date(System.currentTimeMillis() - 1000));
        when(shareMapper.findByShareId("expired")).thenReturn(expired);

        FileShare recycled = new FileShare();
        recycled.setFileId(2);
        recycled.setShareUserId(10);
        UserFile recycledRoot = new UserFile();
        recycledRoot.setId(2);
        recycledRoot.setUserId(10);
        recycledRoot.setStatus(2);
        when(shareMapper.findByShareId("recycled")).thenReturn(recycled);
        when(userFileMapper.selectByPrimaryKey(2)).thenReturn(recycledRoot);

        FileShare publicShare = new FileShare();
        publicShare.setFileId(3);
        publicShare.setShareUserId(10);
        publicShare.setIsPublic(1);
        UserFile publicRoot = new UserFile();
        publicRoot.setId(3);
        publicRoot.setUserId(10);
        publicRoot.setStatus(0);
        when(shareMapper.findByShareId("public")).thenReturn(publicShare);
        when(userFileMapper.selectByPrimaryKey(3)).thenReturn(publicRoot);

        Result expiredResult = service.checkPwd("expired", null);
        Result recycledResult = service.checkPwd("recycled", null);
        Result publicResult = service.checkPwd("public", null);

        assertEquals(NetdiskErrMsgConstant.GET_SHARE_FILE_EXPIRED, expiredResult.getCode());
        assertEquals(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST, recycledResult.getCode());
        assertEquals(NetdiskErrMsgConstant.REQUEST_SUCCESS, publicResult.getCode());
    }

    @Test
    void shouldStreamDirectoryAsZip() throws Exception {
        UserFileMapper userFileMapper = mock(UserFileMapper.class);
        FileOriginMapper fileOriginMapper = mock(FileOriginMapper.class);
        FastDFSClientWrapper fastDFSClientWrapper = mock(FastDFSClientWrapper.class);
        FileDownloadUtil downloadUtil = new FileDownloadUtil();
        ReflectionTestUtils.setField(downloadUtil, "userFileMapper", userFileMapper);
        ReflectionTestUtils.setField(downloadUtil, "fileOriginMapper", fileOriginMapper);
        ReflectionTestUtils.setField(downloadUtil, "fastDFSClientWrapper", fastDFSClientWrapper);

        UserFile root = new UserFile();
        root.setId(1);
        root.setUserId(10);
        root.setFileName("资料");
        root.setIsDir(0);
        root.setStatus(0);
        UserFile child = new UserFile();
        child.setId(2);
        child.setUserId(10);
        child.setParentId(1);
        child.setFileName("说明.txt");
        child.setIsDir(1);
        child.setStatus(0);
        FileOrigin origin = new FileOrigin();
        origin.setFilePath("group1/M00/readme.txt");

        when(userFileMapper.findChildren(10, 1)).thenReturn(Arrays.asList(child));
        when(fileOriginMapper.findByUserFileId(2)).thenReturn(origin);
        doAnswer(invocation -> {
            OutputStream output = invocation.getArgument(1);
            output.write("zip-content".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(fastDFSClientWrapper).downloadFile(eq("group1/M00/readme.txt"), any(OutputStream.class));

        MockHttpServletResponse response = new MockHttpServletResponse();
        downloadUtil.download(root, response);

        String content = null;
        try (ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream(response.getContentAsByteArray()), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("资料/说明.txt".equals(entry.getName())) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[64];
                    int length;
                    while ((length = zip.read(buffer)) != -1) {
                        output.write(buffer, 0, length);
                    }
                    content = new String(output.toByteArray(), StandardCharsets.UTF_8);
                }
            }
        }

        assertEquals("application/zip", response.getContentType());
        assertEquals("zip-content", content);
    }

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
        StorageDeleteTaskMapper storageDeleteTaskMapper = mock(StorageDeleteTaskMapper.class);

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
                fileMediaMapper, fileOriginMapper, fileRecycleMapper, storageDeleteTaskMapper);

        assertEquals(3, paths.size());
        assertTrue(paths.contains("group1/M00/original.dat"));
        assertTrue(paths.contains("group1/M00/preview.pdf"));
        assertTrue(paths.contains("group1/M00/thumb.jpg"));
        verify(fileOriginMapper).deleteByPrimaryKey(10);
        verify(fileOriginMapper, never()).deleteByPrimaryKey(20);
        verify(fileOriginMapper).selectByPrimaryKeyForUpdate(10);
        verify(fileOriginMapper).selectByPrimaryKeyForUpdate(20);
        verify(storageDeleteTaskMapper).insertIgnore("group1/M00/original.dat");
        verify(storageDeleteTaskMapper).insertIgnore("group1/M00/preview.pdf");
        verify(storageDeleteTaskMapper).insertIgnore("group1/M00/thumb.jpg");
        verify(userFileMapper).deleteByIds(fileIds);
        verify(fileRecycleMapper).deleteByPrimaryKey(100);
    }

}
