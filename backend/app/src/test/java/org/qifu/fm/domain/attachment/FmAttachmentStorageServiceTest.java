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

    @Test
    void recognizesOfficeAndArchiveSignatures() throws Exception {
        FmAttachmentStorageService service =
                new FmAttachmentStorageService(temporaryDirectory, 1024);
        Path ole = temporaryDirectory.resolve("legacy-office.bin");
        Files.write(ole, new byte[] {
                (byte) 0xd0, (byte) 0xcf, 0x11, (byte) 0xe0,
                (byte) 0xa1, (byte) 0xb1, 0x1a, (byte) 0xe1});
        Path zip = temporaryDirectory.resolve("openxml.bin");
        Files.write(zip, new byte[] {'P', 'K', 0x03, 0x04, 0, 0, 0, 0});
        Path sevenZip = temporaryDirectory.resolve("archive.7z.bin");
        Files.write(sevenZip, new byte[] {
                0x37, 0x7a, (byte) 0xbc, (byte) 0xaf, 0x27, 0x1c, 0, 0});
        Path rar = temporaryDirectory.resolve("archive.rar.bin");
        Files.write(rar, new byte[] {
                0x52, 0x61, 0x72, 0x21, 0x1a, 0x07, 0x01, 0x00});

        assertTrue(service.hasExpectedSignature(ole, "doc"));
        assertTrue(service.hasExpectedSignature(ole, "xls"));
        assertTrue(service.hasExpectedSignature(ole, "ppt"));
        assertTrue(service.hasExpectedSignature(zip, "docx"));
        assertTrue(service.hasExpectedSignature(zip, "xlsx"));
        assertTrue(service.hasExpectedSignature(zip, "pptx"));
        assertTrue(service.hasExpectedSignature(zip, "zip"));
        assertTrue(service.hasExpectedSignature(sevenZip, "7z"));
        assertTrue(service.hasExpectedSignature(rar, "rar"));
    }
}
