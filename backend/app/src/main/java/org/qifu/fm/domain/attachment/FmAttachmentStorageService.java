package org.qifu.fm.domain.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.UUID;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class FmAttachmentStorageService {

    private static final long DEFAULT_MAX_FILE_SIZE = 8L * 1024L * 1024L;
    private final Path root;
    private final long maxFileSize;

    public FmAttachmentStorageService(Environment environment) {
        this(Path.of(environment.getRequiredProperty("base.uploadDir")),
                environment.getProperty("page.maxUploadSize", Long.class,
                        DEFAULT_MAX_FILE_SIZE));
    }

    FmAttachmentStorageService(Path root, long maxFileSize) {
        this.root = root.toAbsolutePath().normalize().resolve("flowmint").normalize();
        this.maxFileSize = maxFileSize;
    }

    public StoredFile storeTemporary(
            String tenantId, String sessionId, InputStream content, long declaredSize)
            throws ServiceException {
        validateSegment("Tenant", tenantId);
        validateSegment("Upload Session", sessionId);
        validateSize(declaredSize);
        String fileOid = UUID.randomUUID().toString();
        Path target = resolveInsideRoot(tenantId, "temporary", sessionId, fileOid + ".bin");
        return write(target, fileOid, content, declaredSize);
    }

    public Path promote(String tenantId, String sessionId, String fileOid)
            throws ServiceException {
        validateSegment("Tenant", tenantId);
        validateSegment("Upload Session", sessionId);
        validateSegment("File OID", fileOid);
        Path source = resolveInsideRoot(tenantId, "temporary", sessionId, fileOid + ".bin");
        YearMonth month = YearMonth.now(ZoneOffset.UTC);
        Path target = resolveInsideRoot(tenantId, "attachments",
                String.valueOf(month.getYear()), String.format("%02d", month.getMonthValue()),
                fileOid + ".bin");
        try {
            Files.createDirectories(target.getParent());
            return Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailure) {
            try {
                return Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException failure) {
                throw new ServiceException("附件移至正式目錄失敗");
            }
        }
    }

    public boolean hasExpectedSignature(Path path, String extension) throws ServiceException {
        try (InputStream input = Files.newInputStream(path)) {
            byte[] header = input.readNBytes(8);
            return switch (extension) {
                case "pdf" -> startsWith(header,
                        new byte[] {'%', 'P', 'D', 'F', '-'});
                case "jpg", "jpeg" -> startsWith(header,
                        new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff});
                case "png" -> startsWith(header, new byte[] {
                        (byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a});
                case "bmp" -> startsWith(header, new byte[] {'B', 'M'});
                case "doc", "xls", "ppt" -> startsWith(header, new byte[] {
                        (byte) 0xd0, (byte) 0xcf, 0x11, (byte) 0xe0,
                        (byte) 0xa1, (byte) 0xb1, 0x1a, (byte) 0xe1});
                case "docx", "xlsx", "pptx", "zip" -> zipSignature(header);
                case "7z" -> startsWith(header, new byte[] {
                        0x37, 0x7a, (byte) 0xbc, (byte) 0xaf, 0x27, 0x1c});
                case "rar" -> startsWith(header, new byte[] {
                        0x52, 0x61, 0x72, 0x21, 0x1a, 0x07});
                default -> false;
            };
        } catch (IOException exception) {
            throw new ServiceException("無法檢查附件內容");
        }
    }

    private boolean zipSignature(byte[] header) {
        return startsWith(header, new byte[] {'P', 'K', 0x03, 0x04})
                || startsWith(header, new byte[] {'P', 'K', 0x05, 0x06})
                || startsWith(header, new byte[] {'P', 'K', 0x07, 0x08});
    }

    public void deleteTemporary(String tenantId, String sessionId, String fileOid)
            throws ServiceException {
        Path target = resolveInsideRoot(tenantId, "temporary", sessionId, fileOid + ".bin");
        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new ServiceException("刪除暫存附件失敗");
        }
    }

    public byte[] readFormal(String tenantId, String fileOid, Date createdDate)
            throws ServiceException {
        validateSegment("Tenant", tenantId);
        validateSegment("File OID", fileOid);
        if (createdDate == null) throw new ServiceException("附件建立日期不可為空");
        YearMonth month = YearMonth.from(createdDate.toInstant().atZone(ZoneOffset.UTC));
        Path target = resolveInsideRoot(tenantId, "attachments",
                String.valueOf(month.getYear()), String.format("%02d", month.getMonthValue()),
                fileOid + ".bin");
        try {
            return Files.readAllBytes(target);
        } catch (IOException exception) {
            throw new ServiceException("找不到附件實體檔案");
        }
    }

    private StoredFile write(Path target, String fileOid, InputStream content, long declaredSize)
            throws ServiceException {
        Path partial = target.resolveSibling(target.getFileName() + ".part");
        try {
            Files.createDirectories(target.getParent());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long size;
            try (InputStream limited = new SizeLimitedInputStream(content, maxFileSize);
                    DigestInputStream digested = new DigestInputStream(limited, digest)) {
                size = Files.copy(digested, partial, StandardCopyOption.REPLACE_EXISTING);
            }
            if (declaredSize >= 0 && declaredSize != size) {
                throw new ServiceException("附件實際大小與上傳資訊不一致");
            }
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(fileOid, target, size,
                    HexFormat.of().formatHex(digest.digest()));
        } catch (SizeLimitExceededException exception) {
            throw new ServiceException("附件大小超過允許上限");
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new ServiceException("附件儲存失敗");
        } finally {
            try {
                Files.deleteIfExists(partial);
            } catch (IOException ignored) {
                // The scheduled orphan-file cleaner handles an undeletable partial file.
            }
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        return value.length >= prefix.length
                && Arrays.equals(Arrays.copyOf(value, prefix.length), prefix);
    }

    private Path resolveInsideRoot(String first, String... remaining) throws ServiceException {
        Path resolved = root.resolve(first);
        for (String segment : remaining) resolved = resolved.resolve(segment);
        resolved = resolved.toAbsolutePath().normalize();
        if (!resolved.startsWith(root)) throw new ServiceException("附件儲存路徑不合法");
        return resolved;
    }

    private void validateSegment(String label, String value) throws ServiceException {
        if (StringUtils.isBlank(value) || value.contains("/") || value.contains("\\")
                || value.contains("..")) {
            throw new ServiceException(label + " 不合法");
        }
    }

    private void validateSize(long size) throws ServiceException {
        if (size <= 0) throw new ServiceException("附件不可為空");
        if (size > maxFileSize) throw new ServiceException("附件大小超過允許上限");
    }

    public record StoredFile(
            String fileOid,
            Path path,
            long size,
            String sha256) {
    }

    private static final class SizeLimitedInputStream extends InputStream {
        private final InputStream delegate;
        private final long maximum;
        private long count;

        private SizeLimitedInputStream(InputStream delegate, long maximum) {
            this.delegate = delegate;
            this.maximum = maximum;
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value >= 0) increase(1);
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int read = delegate.read(buffer, offset, length);
            if (read > 0) increase(read);
            return read;
        }

        private void increase(int amount) throws SizeLimitExceededException {
            count += amount;
            if (count > maximum) throw new SizeLimitExceededException();
        }
    }

    private static final class SizeLimitExceededException extends IOException {
        private static final long serialVersionUID = 1L;
    }
}
