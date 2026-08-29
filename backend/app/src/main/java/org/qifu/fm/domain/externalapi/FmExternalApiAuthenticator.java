package org.qifu.fm.domain.externalapi;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmApiClient;
import org.qifu.fm.entity.FmApiClientKey;
import org.qifu.fm.service.IFmApiClientKeyService;
import org.qifu.fm.service.IFmApiClientService;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class FmExternalApiAuthenticator {

	private final IFmApiClientKeyService keyService;
	private final IFmApiClientService clientService;
	private final FmExternalApiKeyService apiKeyService;
	private final ObjectMapper objectMapper;

	public FmExternalApiAuthenticator(IFmApiClientKeyService keyService,
			IFmApiClientService clientService, FmExternalApiKeyService apiKeyService,
			ObjectMapper objectMapper) {
		this.keyService = keyService;
		this.clientService = clientService;
		this.apiKeyService = apiKeyService;
		this.objectMapper = objectMapper;
	}

	public FmExternalApiPrincipal authenticate(String plainApiKey, String sourceIp)
			throws ServiceException {
		String keyId = apiKeyService.extractKeyId(plainApiKey);
		if (keyId == null) {
			throw unauthorized();
		}
		FmApiClientKey key = keyService.selectByKeyId(keyId);
		Date now = new Date();
		if (key == null || !"ACTIVE".equals(key.getStatus())
				|| key.getEffectiveFrom() == null || key.getEffectiveFrom().after(now)
				|| key.getExpiresAt() != null && !key.getExpiresAt().after(now)
				|| !apiKeyService.matches(plainApiKey, key.getSecretHash())) {
			throw unauthorized();
		}
		FmApiClient client = loadClient(key);
		Set<String> ipAllowlist = readSet(client.getIpAllowlist());
		if (!FmIpAllowlistMatcher.allows(ipAllowlist, sourceIp)) {
			throw new ServiceException("Source IP is not allowed for this API client.");
		}
		return new FmExternalApiPrincipal(client.getTenantId(), client.getClientId(),
				client.getClientCode(), key.getKeyId(), readSet(client.getAllowedScopes()),
				readSet(client.getAllowedProcessIds()),
				readSet(client.getAllowedInitiatorAccounts()),
				client.getRateLimitPerMinute(), client.getDailyQuota(), sourceIp);
	}

	private FmApiClient loadClient(FmApiClientKey key) throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", key.getTenantId());
		parameters.put("clientId", key.getClientId());
		List<FmApiClient> clients = clientService.selectListByParams(parameters).getValue();
		if (clients.size() != 1 || !"ACTIVE".equals(clients.get(0).getStatus())) {
			throw unauthorized();
		}
		return clients.get(0);
	}

	private Set<String> readSet(String value) throws ServiceException {
		if (StringUtils.isBlank(value)) {
			return Set.of();
		}
		try {
			JsonNode root = objectMapper.readTree(value);
			if (!root.isArray()) {
				throw unauthorized();
			}
			Set<String> values = new LinkedHashSet<>();
			root.forEach(node -> values.add(node.asText()));
			return Set.copyOf(values);
		} catch (ServiceException exception) {
			throw exception;
		} catch (Exception exception) {
			throw unauthorized();
		}
	}

	private ServiceException unauthorized() {
		return new ServiceException("Invalid or inactive external API key.");
	}
}
