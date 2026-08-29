package org.qifu.fm.dto.external;

import java.util.List;

public record FmExternalResponse<T>(
		boolean success,
		String requestId,
		T data,
		Error error) {

	public static <T> FmExternalResponse<T> success(String requestId, T data) {
		return new FmExternalResponse<>(true, requestId, data, null);
	}

	public static <T> FmExternalResponse<T> error(String requestId, String code,
			String message) {
		return new FmExternalResponse<>(false, requestId, null,
				new Error(code, message, List.of()));
	}

	public record Error(String code, String message, List<FieldError> fieldErrors) { }
	public record FieldError(String field, String message) { }
}
