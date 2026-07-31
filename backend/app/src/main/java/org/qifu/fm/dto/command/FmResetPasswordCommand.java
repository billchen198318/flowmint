package org.qifu.fm.dto.command;

public record FmResetPasswordCommand(
		String tenantOid,
		String account,
		String password,
		String confirmPassword) {
}
