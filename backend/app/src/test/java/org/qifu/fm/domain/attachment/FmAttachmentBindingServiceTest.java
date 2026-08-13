package org.qifu.fm.domain.attachment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class FmAttachmentBindingServiceTest {

    @TempDir
    Path temporaryDirectory;

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
        Path formal = storage.promote("A01", "batch", stored.fileOid());
        FmAttachmentBindingService binding = new FmAttachmentBindingService(
                mock(NamedParameterJdbcTemplate.class), storage);
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
