package org.qifu.fm.logic.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.domain.ai.FmAiApiKeyCipher;
import org.qifu.fm.domain.ai.FmAiProviderCatalog;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmAiProviderCommand;
import org.qifu.fm.dto.view.FmAiProviderView;
import org.qifu.fm.entity.FmAiProvider;
import org.qifu.fm.logic.IFmAiProviderLogicService;
import org.qifu.fm.service.IFmAiProviderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmAiProviderLogicServiceImpl implements IFmAiProviderLogicService {

	private final IFmAiProviderService providerService;
	private final FmAiApiKeyCipher apiKeyCipher;
	private final FmTenantAccessGuard tenantAccessGuard;

	public FmAiProviderLogicServiceImpl(IFmAiProviderService providerService,
			FmAiApiKeyCipher apiKeyCipher, FmTenantAccessGuard tenantAccessGuard) {
		this.providerService = providerService;
		this.apiKeyCipher = apiKeyCipher;
		this.tenantAccessGuard = tenantAccessGuard;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmAiProviderView> create(FmAiProviderCommand command)
			throws ServiceException {
		validate(command, true);
		ensureUnique(command.tenantId(), command.providerCode(), null);
		FmAiProvider provider = new FmAiProvider();
		provider.setTenantId(command.tenantId());
		provider.setProviderCode(normalizeCode(command.providerCode()));
		provider.setConfigVersion(1);
		provider.setLockVersion(0);
		apply(provider, command, true);
		clearOtherDefaults(provider);
		providerService.insert(provider);
		return success(view(provider), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmAiProviderView> load(String oid) throws ServiceException {
		FmAiProvider provider = required(oid);
		return success(view(provider), BaseSystemMessage.dataIsExist());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmAiProviderView> update(FmAiProviderCommand command)
			throws ServiceException {
		if (command == null || StringUtils.isBlank(command.oid())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		FmAiProvider provider = required(command.oid());
		if (!Objects.equals(provider.getTenantId(), command.tenantId())
				|| !provider.getProviderCode().equalsIgnoreCase(command.providerCode())
				|| !Objects.equals(provider.getLockVersion(), command.lockVersion())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		validate(command, false);
		apply(provider, command, false);
		clearOtherDefaults(provider);
		providerService.update(provider);
		return success(view(provider), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmAiProviderView> deactivate(String oid) throws ServiceException {
		FmAiProvider provider = required(oid);
		provider.setStatus("INACTIVE");
		provider.setDefaultFlag("N");
		providerService.update(provider);
		return success(view(provider), BaseSystemMessage.updateSuccess());
	}

	@Override
	public FmAiProviderView view(FmAiProvider provider) throws ServiceException {
		boolean configured = StringUtils.isNotBlank(provider.getApiKeyContent());
		return new FmAiProviderView(provider.getOid(), provider.getTenantId(),
				provider.getProviderCode(), provider.getProviderType(),
				provider.getDisplayName(), provider.getBaseUrl(), provider.getModelId(),
				configured, configured ? apiKeyCipher.mask(provider.getApiKeyContent()) : null,
				provider.getTemperature(), provider.getMaxOutputTokens(),
				provider.getTimeoutSeconds(), provider.getDefaultFlag(),
				provider.getConfigVersion(), provider.getStatus(),
				provider.getLastTestStatus(), provider.getLastTestDate(),
				provider.getLockVersion());
	}

	private void validate(FmAiProviderCommand command, boolean apiKeyRequired)
			throws ServiceException {
		if (command == null || StringUtils.isAnyBlank(command.tenantId(),
				command.providerCode(), command.providerType(), command.displayName(),
				command.baseUrl(), command.modelId())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		tenantAccessGuard.requireAccess(command.tenantId());
		FmAiProviderCatalog.requireType(command.providerType());
		FmAiProviderCatalog.requireBaseUrl(command.providerType(), command.baseUrl());
		if (apiKeyRequired && StringUtils.isBlank(command.apiKey())) {
			throw new ServiceException("AI Provider API Key 必填");
		}
		BigDecimal temperature = Objects.requireNonNullElse(
				command.temperature(), new BigDecimal("0.20"));
		int outputTokens = Objects.requireNonNullElse(command.maxOutputTokens(), 2000);
		int timeoutSeconds = Objects.requireNonNullElse(command.timeoutSeconds(), 45);
		if (temperature.compareTo(BigDecimal.ZERO) < 0
				|| temperature.compareTo(new BigDecimal("2.00")) > 0
				|| outputTokens < 256 || outputTokens > 32000
				|| timeoutSeconds < 10 || timeoutSeconds > 120) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private void apply(FmAiProvider provider, FmAiProviderCommand command,
			boolean apiKeyRequired) throws ServiceException {
		provider.setProviderType(FmAiProviderCatalog.requireType(command.providerType()));
		provider.setDisplayName(command.displayName().trim());
		provider.setBaseUrl(FmAiProviderCatalog.requireBaseUrl(
				command.providerType(), command.baseUrl()));
		provider.setModelId(command.modelId().trim());
		if (StringUtils.isNotBlank(command.apiKey())) {
			provider.setApiKeyContent(apiKeyCipher.encrypt(command.apiKey()));
		} else if (apiKeyRequired) {
			throw new ServiceException("AI Provider API Key 必填");
		}
		provider.setTemperature(Objects.requireNonNullElse(
				command.temperature(), new BigDecimal("0.20")));
		provider.setMaxOutputTokens(Objects.requireNonNullElse(
				command.maxOutputTokens(), 2000));
		provider.setTimeoutSeconds(Objects.requireNonNullElse(
				command.timeoutSeconds(), 45));
		provider.setDefaultFlag("Y".equalsIgnoreCase(command.defaultFlag()) ? "Y" : "N");
		provider.setStatus("INACTIVE".equalsIgnoreCase(command.status())
				? "INACTIVE" : "ACTIVE");
	}

	private void clearOtherDefaults(FmAiProvider provider) throws ServiceException {
		if (!"Y".equals(provider.getDefaultFlag())) {
			return;
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", provider.getTenantId());
		for (FmAiProvider current : providerService.selectListByParams(parameters).getValue()) {
			if (!Objects.equals(current.getOid(), provider.getOid())
					&& "Y".equals(current.getDefaultFlag())) {
				current.setDefaultFlag("N");
				providerService.update(current);
			}
		}
	}

	private void ensureUnique(String tenantId, String providerCode, String excludedOid)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("providerCode", normalizeCode(providerCode));
		boolean exists = providerService.selectListByParams(parameters).getValue().stream()
				.anyMatch(provider -> !Objects.equals(provider.getOid(), excludedOid));
		if (exists) {
			throw new ServiceException("同一 Tenant 的 Provider Code 不可重複");
		}
	}

	private FmAiProvider required(String oid) throws ServiceException {
		FmAiProvider provider = providerService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(provider.getTenantId());
		return provider;
	}

	private String normalizeCode(String providerCode) {
		return providerCode.trim().toUpperCase();
	}

	private DefaultResult<FmAiProviderView> success(FmAiProviderView value, String message) {
		DefaultResult<FmAiProviderView> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		result.setMessage(message);
		return result;
	}
}
