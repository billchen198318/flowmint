package org.qifu.fm.dto.command;

import java.util.Date;

public record FmApiKeyIssueCommand(String clientOid, Date expiresAt) {
}
