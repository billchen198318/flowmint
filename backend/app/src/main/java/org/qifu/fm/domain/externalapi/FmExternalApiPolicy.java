package org.qifu.fm.domain.externalapi;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;

public final class FmExternalApiPolicy {

	public static final Set<String> SCOPES = Set.of(
			"org.department.read", "org.employee.read",
			"design.process.read", "design.form.read",
			"runtime.request.submit", "runtime.request.read");
	public static final Set<String> SYSTEM_TYPES = Set.of("ERP", "MES", "HR", "OTHER");

	private FmExternalApiPolicy() {
	}

	public static String requireSystemType(String value) throws ServiceException {
		String normalized = StringUtils.trimToEmpty(value).toUpperCase(Locale.ROOT);
		if (!SYSTEM_TYPES.contains(normalized)) {
			throw new ServiceException("外部系統類型不正確");
		}
		return normalized;
	}

	public static String normalizeClientCode(String value) throws ServiceException {
		String normalized = StringUtils.trimToEmpty(value).toUpperCase(Locale.ROOT);
		if (!normalized.matches("[A-Z][A-Z0-9_]{1,49}")) {
			throw new ServiceException("Client Code 必須為 2 至 50 字的大寫英數字或底線");
		}
		return normalized;
	}

	public static List<String> requireScopes(List<String> values) throws ServiceException {
		if (values == null || values.isEmpty()) {
			throw new ServiceException("至少選擇一項 API Scope");
		}
		LinkedHashSet<String> normalized = new LinkedHashSet<>();
		for (String value : values) {
			String scope = StringUtils.trimToEmpty(value).toLowerCase(Locale.ROOT);
			if (!SCOPES.contains(scope)) {
				throw new ServiceException("不支援的 API Scope：" + scope);
			}
			normalized.add(scope);
		}
		return List.copyOf(normalized);
	}
}
