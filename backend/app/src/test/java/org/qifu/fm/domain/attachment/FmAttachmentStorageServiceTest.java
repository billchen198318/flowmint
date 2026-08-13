package org.qifu.fm.domain.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.qifu.base.exception.ServiceException;

class FmAttachmentStorageServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void storesContentWithUuidNameAndSha256() throws Exception {
        byte[] content = "purchase quotation".getBytes(StandardCharsets.UTF_8);
        FmAttachmentStorageService service =
                new FmAttachmentStorageService(temporaryDirectory, 1024);

        FmAttachmentStorageService.StoredFile stored = service.storeTemporary(
                "A01", "session-1", new ByteArrayInputStream(content), content.length);

        assertTrue(stored.path().startsWith(
                temporaryDirectory.toAbsolutePath().resolve("flowmint")));
        assertTrue(Files.exists(stored.path()));
        assertEquals(content.length, stored.size());
        assertEquals(
                "8049f5d630604f2c057ca6dd3151e9e28a6703a7719eef9e944835d19d8479c8",
                stored.sha256());
    }

    @Test
    void rejectsTraversalEmptyAndOversizedFiles() {
        FmAttachmentStorageService service =
                new FmAttachmentStorageService(temporaryDirectory, 4);

        assertThrows(ServiceException.class, () -> service.storeTemporary(
                "../A01", "session", new ByteArrayInputStream(new byte[] {1}), 1));
        assertThrows(ServiceException.class, () -> service.storeTemporary(
                "A01", "session", new ByteArrayInputStream(new byte[0]), 0));
        assertThrows(ServiceException.class, () -> service.storeTemporary(
                "A01", "session", new ByteArrayInputStream(new byte[5]), 5));
    }

    @Test
    void rejectsTruncatedUpload() {
        FmAttachmentStorageService service =
                new FmAttachmentStorageService(temporaryDirectory, 1024);

        assertThrows(ServiceException.class, () -> service.storeTemporary(
                "A01", "session", new ByteArrayInputStream(new byte[3]), 4));
    }
}
