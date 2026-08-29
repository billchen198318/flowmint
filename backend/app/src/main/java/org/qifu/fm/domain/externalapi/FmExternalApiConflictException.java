package org.qifu.fm.domain.externalapi;

public class FmExternalApiConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final String code;

	public FmExternalApiConflictException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
