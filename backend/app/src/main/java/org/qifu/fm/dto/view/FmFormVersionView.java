package org.qifu.fm.dto.view;

import java.util.Date;

public record FmFormVersionView(
        String oid,
        Integer versionNo,
        String versionStatus,
        String schemaContent,
        String uiSchemaContent,
        String customScriptContent,
        String contentSha256,
        String publishedBy,
        Date publishedDate) {
}
