package group.xuxiake.quartz;

import com.github.tobato.fastdfs.service.FastFileStorageClient;
import group.xuxiake.common.entity.StorageDeleteTask;
import group.xuxiake.common.mapper.FileRecycleMapper;
import group.xuxiake.common.mapper.StorageDeleteTaskMapper;
import group.xuxiake.quartz.job.RecycleMaintenanceTask;
import group.xuxiake.quartz.service.impl.RecyclePurgeService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XNetdiskQuartzApplicationTests {

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
