package org.qifu.fm.dto.external;

import java.time.OffsetDateTime;

public record FmExternalRequest<T>(OffsetDateTime requestTime, T data) {
}
