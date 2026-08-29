package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.externalapi.FmExternalApiKeyService;
import org.qifu.fm.domain.externalapi.FmExternalApiPolicy;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmApiClientCommand;
import org.qifu.fm.dto.command.FmApiKeyIssueCommand;
import org.qifu.fm.dto.command.FmApiKeyRevokeCommand;
import org.qifu.fm.dto.view.FmApiClientKeyView;
import org.qifu.fm.dto.view.FmApiClientView;
import org.qifu.fm.dto.view.FmApiKeyIssueView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmApiClient;
import org.qifu.fm.entity.FmApiClientKey;
import org.qifu.fm.logic.IFmExternalApiManagementLogicService;
import org.qifu.fm.service.IFmApiClientKeyService;
import org.qifu.fm.service.IFmApiClientService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmExternalApiManagementLogicServiceImpl
		implements IFmExternalApiManagementLogicService {

	private static final String ACTIVE = "ACTIVE";
	private final IFmApiClientService clientService;
	private final IFmApiClientKeyService keyService;
	private final FmExternalApiKeyService apiKeyService;
	private final FmTenantAccessGuard tenantAccessGuard;
	private final IFmTenantService tenantService;
	private final ObjectMapper objectMapper;

	public FmExternalApiManagementLogicServiceImpl(IFmApiClientService clientService,
			IFmApiClientKeyService keyService, FmExternalApiKeyService apiKeyService,
			FmTenantAccessGuard tenantAccessGuard, IFmTenantService tenantService,
			ObjectMapper objectMapper) {
		this.clientService = clientService;
		this.keyService = keyService;
		this.apiKeyService = apiKeyService;
		this.tenantAccessGuard = tenantAccessGuard;
		this.tenantService = tenantService;
		this.objectMapper = objectMapper;
	}

	@Override
	public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("status", ACTIVE);
		var tenants = tenantService.selectListByParams(parameters, "TENANT_CODE", "ASC")
				.getValue();
		if (!UserUtils.isAdmin()) {
			var accessibleTenantIds = tenantAccessGuard.accessibleTenantIds();
			tenants = tenants.stream()
					.filter(value -> accessibleTenantIds.contains(value.getTenantId()))
					.toList();
		}
		return success(tenants.stream().map(value -> new FmOptionView(value.getTenantId(),
				value.getTenantCode() + " / " + value.getTenantName())).toList(), null);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApiClientView> create(FmApiClientCommand command)
			throws ServiceException {
		validate(command);
		String code = FmExternalApiPolicy.normalizeClientCode(command.clientCode());
		ensureUnique(command.tenantId(), code, null);
		FmApiClient client = new FmApiClient();
		client.setTenantId(command.tenantId());
		client.setClientId(UUID.randomUUID().toString());
		client.setClientCode(code);
		client.setLockVersion(0);
		apply(client, command);
		clientService.insert(client);
		return success(view(requiredClient(client.getOid())), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmApiClientView> load(String oid) throws ServiceException {
		return success(view(requiredClient(oid)), BaseSystemMessage.dataIsExist());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApiClientView> update(FmApiClientCommand command)
			throws ServiceException {
		if (command == null || StringUtils.isBlank(command.oid())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		FmApiClient client = requiredClient(command.oid());
		validate(command);
		String code = FmExternalApiPolicy.normalizeClientCode(command.clientCode());
		if (!Objects.equals(client.getTenantId(), command.tenantId())
				|| !client.getClientCode().equalsIgnoreCase(code)
				|| !Objects.equals(client.getLockVersion(), command.lockVersion())
				|| !Objects.equals(client.getStatus(), command.status())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		apply(client, command);
		clientService.update(client);
		return success(view(requiredClient(client.getOid())), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApiClientView> deactivate(String oid) throws ServiceException {
		FmApiClient client = requiredClient(oid);
		client.setStatus("INACTIVE");
		clientService.update(client);
		for (FmApiClientKey key : findKeys(client)) {
			if (ACTIVE.equals(key.getStatus())) {
				keyService.revoke(key, "API client deactivated");
			}
		}
		return success(view(requiredClient(oid)), BaseSystemMessage.updateSuccess());
	}

	@Override
	public DefaultResult<List<FmApiClientKeyView>> keys(String clientOid)
			throws ServiceException {
		FmApiClient client = requiredClient(clientOid);
		return success(findKeys(client).stream().map(this::keyView).toList(),
				BaseSystemMessage.dataIsExist());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApiKeyIssueView> issueKey(FmApiKeyIssueCommand command)
			throws ServiceException {
		if (command == null || StringUtils.isBlank(command.clientOid())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		FmApiClient client = requiredClient(command.clientOid());
		if (!ACTIVE.equals(client.getStatus())
				|| command.expiresAt() != null && !command.expiresAt().after(new Date())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		return issue(client, command.expiresAt());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApiKeyIssueView> rotateKey(FmApiKeyIssueCommand command)
			throws ServiceException {
		if (command == null || StringUtils.isBlank(command.clientOid())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		FmApiClient client = requiredClient(command.clientOid());
		for (FmApiClientKey key : findKeys(client)) {
			if (ACTIVE.equals(key.getStatus())) {
				keyService.revoke(key, "API key rotated");
			}
		}
		return issueKey(command);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApiClientKeyView> revokeKey(FmApiKeyRevokeCommand command)
			throws ServiceException {
		if (command == null || StringUtils.isBlank(command.keyOid())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		FmApiClientKey key = requiredKey(command.keyOid());
		if (ACTIVE.equals(key.getStatus())) {
			keyService.revoke(key, StringUtils.defaultIfBlank(command.reason(),
					"Revoked by administrator"));
		}
		return success(keyView(requiredKey(command.keyOid())),
				BaseSystemMessage.updateSuccess());
	}

	@Override
	public FmApiClientView view(FmApiClient client) throws ServiceException {
		return new FmApiClientView(client.getOid(), client.getTenantId(),
				client.getClientId(), client.getClientCode(), client.getClientName(),
				client.getSystemType(), client.getDescription(),
				readList(client.getAllowedScopes()), readList(client.getAllowedProcessIds()),
				readList(client.getAllowedInitiatorAccounts()),
				readList(client.getIpAllowlist()), client.getRateLimitPerMinute(),
				client.getDailyQuota(), client.getStatus(), client.getLockVersion());
	}

	private DefaultResult<FmApiKeyIssueView> issue(FmApiClient client, Date expiresAt)
			throws ServiceException {
		FmExternalApiKeyService.GeneratedKey generated = apiKeyService.generate();
		Date now = new Date();
		FmApiClientKey key = new FmApiClientKey();
		key.setTenantId(client.getTenantId());
		key.setClientId(client.getClientId());
		key.setKeyId(generated.keyId());
		key.setKeyPrefix(generated.prefix());
		key.setKeyLastFour(generated.lastFour());
		key.setSecretHash(generated.secretHash());
		key.setEffectiveFrom(now);
		key.setExpiresAt(expiresAt);
		key.setFailedCount(0);
		key.setStatus(ACTIVE);
		key.setLockVersion(0);
		keyService.insert(key);
		FmApiKeyIssueView value = new FmApiKeyIssueView(key.getOid(), client.getClientId(),
				generated.keyId(), generated.plainText(), now, expiresAt,
				"API key is displayed only once. Store it securely.");
		return success(value, BaseSystemMessage.insertSuccess());
	}

	private void validate(FmApiClientCommand command) throws ServiceException {
		if (command == null || StringUtils.isAnyBlank(command.tenantId(),
				command.clientCode(), command.clientName(), command.systemType())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		tenantAccessGuard.requireAccess(command.tenantId());
		FmExternalApiPolicy.requireSystemType(command.systemType());
		FmExternalApiPolicy.requireScopes(command.allowedScopes());
		int minuteLimit = Objects.requireNonNullElse(command.rateLimitPerMinute(), 60);
		int dailyQuota = Objects.requireNonNullElse(command.dailyQuota(), 10000);
		if (command.clientName().trim().length() > 100 || minuteLimit < 1
				|| minuteLimit > 10000 || dailyQuota < 1 || dailyQuota > 10000000) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private void apply(FmApiClient client, FmApiClientCommand command)
			throws ServiceException {
		client.setClientName(command.clientName().trim());
		client.setSystemType(FmExternalApiPolicy.requireSystemType(command.systemType()));
		client.setDescription(StringUtils.trimToNull(command.description()));
		client.setAllowedScopes(writeList(
				FmExternalApiPolicy.requireScopes(command.allowedScopes())));
		client.setAllowedProcessIds(writeList(clean(command.allowedProcessIds())));
		client.setAllowedInitiatorAccounts(
				writeList(clean(command.allowedInitiatorAccounts())));
		client.setIpAllowlist(writeList(clean(command.ipAllowlist())));
		client.setRateLimitPerMinute(
				Objects.requireNonNullElse(command.rateLimitPerMinute(), 60));
		client.setDailyQuota(Objects.requireNonNullElse(command.dailyQuota(), 10000));
		client.setStatus("INACTIVE".equalsIgnoreCase(command.status())
				? "INACTIVE" : ACTIVE);
	}

	private void ensureUnique(String tenantId, String clientCode, String excludedOid)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("clientCode", clientCode);
		boolean exists = clientService.selectListByParams(parameters).getValue().stream()
				.anyMatch(client -> !Objects.equals(client.getOid(), excludedOid));
		if (exists) {
			throw new ServiceException("Client Code already exists in this tenant.");
		}
	}

	private FmApiClient requiredClient(String oid) throws ServiceException {
		FmApiClient client = clientService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(client.getTenantId());
		return client;
	}

	private FmApiClientKey requiredKey(String oid) throws ServiceException {
		FmApiClientKey key = keyService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(key.getTenantId());
		return key;
	}

	private List<FmApiClientKey> findKeys(FmApiClient client) throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", client.getTenantId());
		parameters.put("clientId", client.getClientId());
		return keyService.selectListByParams(parameters, "CDATE", "DESC").getValue();
	}

	private FmApiClientKeyView keyView(FmApiClientKey key) {
		return new FmApiClientKeyView(key.getOid(), key.getClientId(), key.getKeyId(),
				key.getKeyPrefix() + "..." + key.getKeyLastFour(), key.getEffectiveFrom(),
				key.getExpiresAt(), key.getRevokedAt(), key.getRevokedBy(),
				key.getRevokeReason(), key.getLastUsedAt(), key.getLastSourceIp(),
				key.getStatus());
	}

	private List<String> clean(List<String> values) {
		if (values == null) {
			return List.of();
		}
		LinkedHashSet<String> result = new LinkedHashSet<>();
		values.stream().map(StringUtils::trimToEmpty).filter(StringUtils::isNotBlank)
				.forEach(result::add);
		return List.copyOf(result);
	}

	private String writeList(List<String> values) throws ServiceException {
		try {
			return objectMapper.writeValueAsString(values);
		} catch (Exception exception) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private List<String> readList(String value) throws ServiceException {
		if (StringUtils.isBlank(value)) {
			return List.of();
		}
		try {
			JsonNode root = objectMapper.readTree(value);
			List<String> values = new ArrayList<>();
			root.forEach(node -> values.add(node.asText()));
			return List.copyOf(values);
		} catch (Exception exception) {
			throw new ServiceException("Invalid API client policy JSON.");
		}
	}

	private <T> DefaultResult<T> success(T value, String message) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		result.setMessage(message);
		return result;
	}
}
