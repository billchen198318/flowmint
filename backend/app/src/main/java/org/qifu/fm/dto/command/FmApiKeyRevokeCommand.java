package org.qifu.fm.dto.command;

public record FmApiKeyRevokeCommand(String keyOid, String reason) {
}
