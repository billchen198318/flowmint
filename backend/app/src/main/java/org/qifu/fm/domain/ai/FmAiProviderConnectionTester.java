package org.qifu.fm.domain.ai;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class FmAiProviderConnectionTester {

	private static final int MAX_RESPONSE_BYTES = 64 * 1024;

	public void test(FmAiProviderConfig config) throws ServiceException {
		FmAiProviderCatalog.requireBaseUrl(config.providerType(), config.baseUrl());
		HttpRequest request = request(config);
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(config.timeoutSeconds()))
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
		try {
			HttpResponse<InputStream> response = client.send(request,
					HttpResponse.BodyHandlers.ofInputStream());
			try (InputStream body = response.body()) {
				if (body.readNBytes(MAX_RESPONSE_BYTES + 1).length > MAX_RESPONSE_BYTES) {
					throw new ServiceException("AI Provider 回應超過安全上限");
				}
			}
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new ServiceException("AI Provider 連線測試失敗，HTTP "
						+ response.statusCode());
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new ServiceException("AI Provider 連線測試已中斷");
		} catch (IOException exception) {
			throw new ServiceException("AI Provider 連線測試失敗");
		}
	}

	HttpRequest request(FmAiProviderConfig config) throws ServiceException {
		String type = FmAiProviderCatalog.requireType(config.providerType());
		String baseUrl = stripTrailingSlash(config.baseUrl());
		String path = "GEMINI".equals(type) ? "/v1beta/models" : "/models";
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
				.timeout(Duration.ofSeconds(config.timeoutSeconds()))
				.GET();
		if ("GEMINI".equals(type)) {
			builder.header("x-goog-api-key", config.apiKey());
		} else {
			builder.header("Authorization", "Bearer " + config.apiKey());
		}
		return builder.build();
	}

	private String stripTrailingSlash(String value) {
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}
}
