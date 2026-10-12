package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.qifu.fm.domain.attachment.FmAttachmentStorageService;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.mockito.ArgumentCaptor;
import java.util.List;

class FmAttachmentUploadRollbackTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void uploadLocksOpenSessionBeforeWritingFile() throws Exception {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForList(anyString(), any(MapSqlParameterSource.class),
                eq(String.class))).thenReturn(List.of());
        FmAttachmentUploadLogicServiceImpl logic = new FmAttachmentUploadLogicServiceImpl(
                null, null, null, null, null, null, null, jdbc);
        assertThrows(org.qifu.base.exception.ServiceException.class,
                () -> logic.lockOpenSessionForUpload("A01", "batch", "owner"));
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForList(sql.capture(), any(MapSqlParameterSource.class),
                eq(String.class));
        assertTrue(sql.getValue().contains("FOR UPDATE"));
        assertTrue(sql.getValue().contains("SESSION_STATUS = 'OPEN'"));
    }

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rollbackRemovesStoredFileButCommitKeepsIt() throws Exception {
        FmAttachmentStorageService storage =
                new FmAttachmentStorageService(new MockEnvironment()
                        .withProperty("base.uploadDir", temporaryDirectory.toString()));
        FmAttachmentUploadLogicServiceImpl logic = new FmAttachmentUploadLogicServiceImpl(
                null, null, null, null, null, storage, null, null);
        byte[] content = "%PDF-test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var rolledBack = storage.storeTemporary(
                "A01", "batch", new ByteArrayInputStream(content), content.length);
        TransactionSynchronizationManager.initSynchronization();
        logic.registerUploadRollbackCleanup("A01", "batch", rolledBack.fileOid());
        TransactionSynchronizationManager.getSynchronizations().forEach(sync ->
                sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        assertFalse(Files.exists(rolledBack.path()));
        TransactionSynchronizationManager.clearSynchronization();

        var committed = storage.storeTemporary(
                "A01", "batch", new ByteArrayInputStream(content), content.length);
        TransactionSynchronizationManager.initSynchronization();
        logic.registerUploadRollbackCleanup("A01", "batch", committed.fileOid());
        TransactionSynchronizationManager.getSynchronizations().forEach(sync ->
                sync.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
        assertTrue(Files.exists(committed.path()));
    }
}
