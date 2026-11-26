package group.xuxiake.quartz;

import com.github.tobato.fastdfs.service.FastFileStorageClient;
import group.xuxiake.common.entity.StorageDeleteTask;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.StorageDeleteTaskMapper;
import group.xuxiake.common.mapper.UserFileMapper;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.quartz.job.RecycleMaintenanceTask;
import group.xuxiake.quartz.service.impl.RecyclePurgeService;
import group.xuxiake.quartz.service.impl.UserCapacityReconcileService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XNetdiskQuartzApplicationTests {

    @Test
    void shouldReconcileUserCapacityFromNormalFiles() {
        UserMapper userMapper = mock(UserMapper.class);
        UserFileMapper userFileMapper = mock(UserFileMapper.class);
        UserCapacityReconcileService service = new UserCapacityReconcileService();
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        ReflectionTestUtils.setField(service, "userFileMapper", userFileMapper);
        User user = new User();
        user.setId(10);
        user.setUsedMemory(100L);
        when(userMapper.selectByPrimaryKeyForUpdate(10)).thenReturn(user);
        when(userFileMapper.countNormalFilesWithoutOrigin(10)).thenReturn(0);
        when(userFileMapper.sumNormalFileSizeByUserId(10)).thenReturn(250L);

        service.reconcile(10);

        verify(userMapper).updateByPrimaryKeySelective(argThat(update ->
                Integer.valueOf(10).equals(update.getId()) && Long.valueOf(250L).equals(update.getUsedMemory())));
    }

    @Test
    void shouldCompensateExpiredRecycleRecords() {
        FileRecycleMapper recycleMapper = mock(FileRecycleMapper.class);
        RecyclePurgeService purgeService = mock(RecyclePurgeService.class);
        RecycleMaintenanceTask task = new RecycleMaintenanceTask();
        ReflectionTestUtils.setField(task, "fileRecycleMapper", recycleMapper);
        ReflectionTestUtils.setField(task, "recyclePurgeService", purgeService);
        when(recycleMapper.findExpiredActiveIds(any(), eq(100))).thenReturn(Arrays.asList(1, 2));

        task.purgeExpiredRecycleRecords();

        verify(purgeService).purge(1);
        verify(purgeService).purge(2);
    }

    @Test
    void shouldDeleteCompletedStorageRetryTask() {
        StorageDeleteTaskMapper taskMapper = mock(StorageDeleteTaskMapper.class);
        FastFileStorageClient storageClient = mock(FastFileStorageClient.class);
        RecycleMaintenanceTask task = new RecycleMaintenanceTask();
        ReflectionTestUtils.setField(task, "storageDeleteTaskMapper", taskMapper);
        ReflectionTestUtils.setField(task, "storageClient", storageClient);
        StorageDeleteTask deleteTask = new StorageDeleteTask();
        deleteTask.setId(5);
        deleteTask.setStoragePath("group1/M00/file.dat");
        deleteTask.setRetryCount(0);
        when(taskMapper.findDueTasks(any(), eq(100))).thenReturn(Collections.singletonList(deleteTask));
        when(taskMapper.claim(eq(5), any(), any())).thenReturn(1);

        task.retryStorageDeletes();

        verify(storageClient).deleteFile("group1/M00/file.dat");
        verify(taskMapper).deleteByPrimaryKey(5);
    }

    @Test
    void shouldRescheduleFailedStorageDelete() {
        StorageDeleteTaskMapper taskMapper = mock(StorageDeleteTaskMapper.class);
        FastFileStorageClient storageClient = mock(FastFileStorageClient.class);
        RecycleMaintenanceTask task = new RecycleMaintenanceTask();
        ReflectionTestUtils.setField(task, "storageDeleteTaskMapper", taskMapper);
        ReflectionTestUtils.setField(task, "storageClient", storageClient);
        StorageDeleteTask deleteTask = new StorageDeleteTask();
        deleteTask.setId(6);
        deleteTask.setStoragePath("group1/M00/retry.dat");
        deleteTask.setRetryCount(1);
        when(taskMapper.findDueTasks(any(), eq(100))).thenReturn(Collections.singletonList(deleteTask));
        when(taskMapper.claim(eq(6), any(), any())).thenReturn(1);
        doThrow(new RuntimeException("storage unavailable"))
                .when(storageClient).deleteFile("group1/M00/retry.dat");

        task.retryStorageDeletes();

        verify(taskMapper).markRetry(eq(6), any(), eq("storage unavailable"));
        verify(taskMapper, never()).deleteByPrimaryKey(6);
    }

}
