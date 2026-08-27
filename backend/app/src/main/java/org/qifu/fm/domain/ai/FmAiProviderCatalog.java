package org.qifu.fm.domain.ai;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;

public final class FmAiProviderCatalog {

	private static final Map<String, Set<String>> ALLOWED_HOSTS = Map.of(
			"OPENAI", Set.of("api.openai.com"),
			"GEMINI", Set.of("generativelanguage.googleapis.com"),
			"GROQ", Set.of("api.groq.com"),
			"OPENROUTER", Set.of("openrouter.ai"));

	private FmAiProviderCatalog() {
	}

	public static String requireType(String providerType) throws ServiceException {
		String normalized = StringUtils.upperCase(StringUtils.trim(providerType));
		if (!ALLOWED_HOSTS.containsKey(normalized)) {
			throw new ServiceException("不支援的 AI Provider 類型");
		}
		return normalized;
	}

	public static String requireBaseUrl(String providerType, String baseUrl)
			throws ServiceException {
		try {
			URI uri = URI.create(StringUtils.trim(baseUrl));
			String host = StringUtils.lowerCase(uri.getHost());
			boolean valid = "https".equalsIgnoreCase(uri.getScheme());
			valid = valid && StringUtils.isNotBlank(host);
			valid = valid && ALLOWED_HOSTS.get(requireType(providerType)).contains(host);
			valid = valid && uri.getUserInfo() == null;
			valid = valid && StringUtils.isBlank(uri.getQuery());
			valid = valid && StringUtils.isBlank(uri.getFragment());
			if (!valid) {
				throw new ServiceException("AI Provider Base URL 不在允許清單");
			}
			return uri.toString();
		} catch (IllegalArgumentException exception) {
			throw new ServiceException("AI Provider Base URL 格式錯誤");
		}
	}
}
