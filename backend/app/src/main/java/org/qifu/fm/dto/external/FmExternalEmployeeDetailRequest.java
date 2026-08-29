package org.qifu.fm.dto.external;

import java.time.OffsetDateTime;

public record FmExternalEmployeeDetailRequest(String account, OffsetDateTime effectiveAt) { }
