package org.qifu.fm.domain.attachment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

class FmAttachmentBindingServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void formalPathUsesPersistedCreationMonthAcrossUtcBoundary() throws Exception {
        FmAttachmentStorageService storage =
                new FmAttachmentStorageService(temporaryDirectory, 1024);
        byte[] content = "%PDF-month".getBytes(StandardCharsets.UTF_8);
        var stored = storage.storeTemporary(
                "A01", "batch", new ByteArrayInputStream(content), content.length);
        Date createdDate = Date.from(Instant.parse("2025-01-31T23:59:59Z"));

        Path formal = storage.promote("A01", "batch", stored.fileOid(), createdDate);

        assertTrue(formal.toString().replace('\\', '/').contains("/attachments/2025/01/"));
        assertTrue(java.util.Arrays.equals(
                content, storage.readFormal("A01", stored.fileOid(), createdDate)));
    }

    @Test
    void bindingLocksOpenSessionBeforeReadingFiles() throws Exception {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForList(anyString(), any(MapSqlParameterSource.class),
                eq(String.class))).thenReturn(List.of());
        FmAttachmentBindingService binding = new FmAttachmentBindingService(
                jdbc, mock(FmAttachmentStorageService.class), new ObjectMapper());
        assertThrows(org.qifu.base.exception.ServiceException.class,
                () -> binding.bind("A01", "batch", "owner", "form", 1,
                        "data", new Date(), "{\"components\":[]}", Map.of()));
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForList(sql.capture(), any(MapSqlParameterSource.class),
                eq(String.class));
        assertTrue(sql.getValue().contains("FOR UPDATE"));
        assertTrue(sql.getValue().contains("SESSION_STATUS = 'OPEN'"));
    }

    @Test
    void oldTemporaryFileCanBeFoundAndQuarantined() throws Exception {
        FmAttachmentStorageService storage =
                new FmAttachmentStorageService(temporaryDirectory, 1024);
        byte[] content = "%PDF-orphan".getBytes(StandardCharsets.UTF_8);
        var stored = storage.storeTemporary(
                "A01", "batch", new ByteArrayInputStream(content), content.length);
        Files.setLastModifiedTime(stored.path(),
                FileTime.from(Instant.now().minus(25, ChronoUnit.HOURS)));
        var found = storage.findTemporaryFilesOlderThan(
                Instant.now().minus(24, ChronoUnit.HOURS), 10);
        assertEquals(1, found.size());
        assertEquals(stored.fileOid(), found.get(0).fileOid());
        Path quarantined = storage.quarantineTemporary(found.get(0));
        assertFalse(Files.exists(stored.path()));
        assertTrue(Files.exists(quarantined));
    }

    @Test
    void temporaryFileDeletionRunsOnlyAfterCommit() throws Exception {
        FmAttachmentStorageService storage =
                new FmAttachmentStorageService(temporaryDirectory, 1024);
        byte[] content = "%PDF-delete".getBytes(StandardCharsets.UTF_8);
        var rolledBack = storage.storeTemporary(
                "A01", "batch", new ByteArrayInputStream(content), content.length);
        TransactionSynchronizationManager.initSynchronization();
        storage.deleteTemporaryAfterCommit("A01", "batch", rolledBack.fileOid());
        TransactionSynchronizationManager.getSynchronizations().forEach(sync ->
                sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        assertTrue(Files.exists(rolledBack.path()));
        TransactionSynchronizationManager.clearSynchronization();

        var committed = storage.storeTemporary(
                "A01", "batch", new ByteArrayInputStream(content), content.length);
        TransactionSynchronizationManager.initSynchronization();
        storage.deleteTemporaryAfterCommit("A01", "batch", committed.fileOid());
        TransactionSynchronizationManager.getSynchronizations().forEach(
                TransactionSynchronization::afterCommit);
        assertFalse(Files.exists(committed.path()));
    }

    @Test
    void submittedAttachmentIdsMustMatchSessionFilesAndFieldKeys() throws Exception {
        FmAttachmentBindingService binding = new FmAttachmentBindingService(
                mock(NamedParameterJdbcTemplate.class),
                mock(FmAttachmentStorageService.class), new ObjectMapper());
        String schema = """
                {"components":[{"type":"file","key":"invoice"},
                  {"type":"container","key":"details","components":[
                    {"type":"file","key":"receipt"}]}]}
                """;
        Set<String> submitted = binding.submittedFiles(schema,
                Map.of("invoice", List.of("A"),
                        "details", Map.of("receipt", List.of("B"))));
        assertEquals(Set.of("invoice\u0000A", "receipt\u0000B"), submitted);
        binding.assertMatchingFiles(submitted, List.of(
                Map.of("FIELD_KEY", "invoice", "ATTACHMENT_ID", "A"),
                Map.of("FIELD_KEY", "receipt", "ATTACHMENT_ID", "B")));
        assertThrows(org.qifu.base.exception.ServiceException.class,
                () -> binding.assertMatchingFiles(submitted, List.of(
                        Map.of("FIELD_KEY", "invoice", "ATTACHMENT_ID", "A"))));
        assertThrows(org.qifu.base.exception.ServiceException.class,
                () -> binding.assertMatchingFiles(submitted, List.of(
                        Map.of("FIELD_KEY", "wrong", "ATTACHMENT_ID", "A"),
                        Map.of("FIELD_KEY", "receipt", "ATTACHMENT_ID", "B"))));
        assertThrows(org.qifu.base.exception.ServiceException.class,
                () -> binding.assertMatchingFiles(submitted, List.of(
                        Map.of("FIELD_KEY", "invoice", "ATTACHMENT_ID", "A"),
                        Map.of("FIELD_KEY", "receipt", "ATTACHMENT_ID", "B"),
                        Map.of("FIELD_KEY", "receipt", "ATTACHMENT_ID", "C"))));
        assertThrows(org.qifu.base.exception.ServiceException.class,
                () -> binding.bind("tenant", null, "owner", "form", 1,
                        "data", new Date(), schema,
                        Map.of("invoice", List.of("A"))));

        String gridSchema = """
                {"components":[{"type":"datagrid","key":"rows","components":[
                  {"type":"file","key":"proof"}]}]}
                """;
        assertEquals(Set.of("proof\u0000A", "proof\u0000B"),
                binding.submittedFiles(gridSchema, Map.of("rows", List.of(
                        Map.of("proof", List.of("A")),
                        Map.of("proof", List.of("B"))))));
    }

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void restoresPromotedFileWhenTransactionRollsBack() throws Exception {
        byte[] content = "%PDF-rollback".getBytes(StandardCharsets.UTF_8);
        FmAttachmentStorageService storage =
                new FmAttachmentStorageService(temporaryDirectory, 1024);
        var stored = storage.storeTemporary(
                "A01", "batch", new ByteArrayInputStream(content), content.length);
        Path formal = storage.promote("A01", "batch", stored.fileOid(), new Date());
        FmAttachmentBindingService binding = new FmAttachmentBindingService(
                mock(NamedParameterJdbcTemplate.class), storage, new ObjectMapper());
        TransactionSynchronizationManager.initSynchronization();

        binding.registerRollbackCompensation("A01", "batch", stored.fileOid(), formal);
        TransactionSynchronizationManager.getSynchronizations().forEach(
                synchronization -> synchronization.afterCompletion(
                        TransactionSynchronization.STATUS_ROLLED_BACK));

        assertFalse(Files.exists(formal));
        Path restored = temporaryDirectory.resolve("flowmint/A01/temporary/batch/")
                .resolve(stored.fileOid() + ".bin");
        assertTrue(Files.exists(restored));
    }
}
