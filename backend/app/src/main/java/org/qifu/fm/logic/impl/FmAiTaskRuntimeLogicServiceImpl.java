package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.ai.FmAiAccessLedger;
import org.qifu.fm.domain.ai.FmAiAnalysisRequest;
import org.qifu.fm.domain.ai.FmAiAnalysisResponse;
import org.qifu.fm.domain.ai.FmAiApiKeyCipher;
import org.qifu.fm.domain.ai.FmAiContentHashService;
import org.qifu.fm.domain.ai.FmAiContextBuilder;
import org.qifu.fm.domain.ai.FmAiProviderClientRegistry;
import org.qifu.fm.domain.ai.FmAiProviderConfig;
import org.qifu.fm.dto.command.FmAiAnalysisCommand;
import org.qifu.fm.dto.view.FmAiAnalysisView;
import org.qifu.fm.dto.view.FmAiProviderOptionView;
import org.qifu.fm.dto.view.FmTaskDetailView;
import org.qifu.fm.entity.FmAiAnalysis;
import org.qifu.fm.entity.FmAiProvider;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.logic.IFmAiTaskRuntimeLogicService;
import org.qifu.fm.logic.IFmTaskRuntimeLogicService;
import org.qifu.fm.service.IFmAiAnalysisService;
import org.qifu.fm.service.IFmAiProviderService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmAiTaskRuntimeLogicServiceImpl implements IFmAiTaskRuntimeLogicService {

	private static final int PROMPT_VERSION = 1;
	private static final String DISCLAIMER =
			"AI 內容僅供簽核參考，請依原始表單、附件與組織規範自行判斷。";
	private final IFmTaskRuntimeLogicService taskRuntimeLogicService;
	private final IFmAiProviderService providerService;
	private final IFmProcessInstanceService processInstanceService;
	private final IFmFormDataService formDataService;
	private final IFmAiAnalysisService analysisService;
	private final FmAiContextBuilder contextBuilder;
	private final FmAiContentHashService contentHashService;
	private final FmAiApiKeyCipher apiKeyCipher;
	private final FmAiProviderClientRegistry clientRegistry;
	private final FmAiAccessLedger accessLedger;
	private final ObjectMapper objectMapper;

	public FmAiTaskRuntimeLogicServiceImpl(IFmTaskRuntimeLogicService taskRuntimeLogicService,
			IFmAiProviderService providerService,
			IFmProcessInstanceService processInstanceService,
			IFmFormDataService formDataService,
			IFmAiAnalysisService analysisService, FmAiContextBuilder contextBuilder,
			FmAiContentHashService contentHashService, FmAiApiKeyCipher apiKeyCipher,
			FmAiProviderClientRegistry clientRegistry, FmAiAccessLedger accessLedger,
			ObjectMapper objectMapper) {
		this.taskRuntimeLogicService = taskRuntimeLogicService;
		this.providerService = providerService;
		this.processInstanceService = processInstanceService;
		this.formDataService = formDataService;
		this.analysisService = analysisService;
		this.contextBuilder = contextBuilder;
		this.contentHashService = contentHashService;
		this.apiKeyCipher = apiKeyCipher;
		this.clientRegistry = clientRegistry;
		this.accessLedger = accessLedger;
		this.objectMapper = objectMapper;
	}

	@Override
	public DefaultResult<List<FmAiProviderOptionView>> providerOptions(
			String tenantId, String taskId) throws ServiceException {
		FmTaskDetailView task = authorizedTask(tenantId, taskId);
		if (task.correctionTask()) {
			throw new ServiceException("補件工作不提供 AI 簽核解說");
		}
		Map<String, Object> params = params("tenantId", tenantId);
		params.put("status", "ACTIVE");
		return success(providerService
				.selectListByParams(params, "DEFAULT_FLAG DESC, PROVIDER_CODE", "ASC")
				.getValue().stream()
				.filter(p -> StringUtils.isNotBlank(p.getApiKeyContent()))
				.map(this::option).toList());
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DefaultResult<FmAiAnalysisView> analyze(String tenantId,
			FmAiAnalysisCommand command) throws ServiceException {
		if (command == null || StringUtils.isAnyBlank(command.taskId(), command.providerCode())) {
			throw new ServiceException("缺少 Task 或 AI Provider");
		}
		FmTaskDetailView detail = authorizedTask(tenantId, command.taskId());
		if (detail.correctionTask()) {
			throw new ServiceException("補件工作不提供 AI 簽核解說");
		}
		String actor = UserUtils.getCurrentUser().getUsername();
		record(tenantId, null, command.taskId(), actor, "CLICK", "SUCCESS", null);
		FmProcessInstance process = process(tenantId, detail.task().processInstanceId());
		FmFormData formData = formData(tenantId, process.getFormDataId());
		FmAiProvider provider = provider(tenantId, command.providerCode());
		JsonNode context = contextBuilder.build(detail);
		String hash = contentHashService.hash(tenantId, process.getProcessInstanceId(),
				command.taskId(), formData.getRevisionNo(), context,
				provider.getConfigVersion(), PROMPT_VERSION);
		if (!Boolean.TRUE.equals(command.forceRefresh())) {
			FmAiAnalysis cached = analysisService.findLatestSucceeded(tenantId, hash);
			if (cached != null) {
				record(tenantId, cached.getAnalysisId(), command.taskId(), actor,
						"CACHE_HIT", "SUCCESS", null);
				return success(view(cached, true));
			}
		}
		FmAiAnalysis analysis = createValue(tenantId, detail, process, formData,
				provider, actor, hash, Boolean.TRUE.equals(command.forceRefresh()));
		startWithRetry(analysis);
		record(tenantId, analysis.getAnalysisId(), command.taskId(), actor,
				"START", "SUCCESS", null);
		long startedAt = System.currentTimeMillis();
		try {
			FmAiAnalysisResponse response = clientRegistry.required(provider.getProviderType())
					.analyze(config(provider),
							new FmAiAnalysisRequest(context, "zh-TW", PROMPT_VERSION));
			analysis.setCompleteDate(new Date());
			analysis.setElapsedMillis(System.currentTimeMillis() - startedAt);
			analysis.setInputTokens(response.inputTokens());
			analysis.setOutputTokens(response.outputTokens());
			analysis.setResultContent(objectMapper.writeValueAsString(response.result()));
			analysisService.complete(analysis);
			record(tenantId, analysis.getAnalysisId(), command.taskId(), actor,
					"SUCCEEDED", "SUCCESS", null);
			return success(view(analysis, false));
		} catch (Exception exception) {
			fail(analysis, startedAt);
			record(tenantId, analysis.getAnalysisId(), command.taskId(), actor,
					"FAILED", "FAILURE", "AI_PROVIDER_ERROR");
			if (exception instanceof ServiceException serviceException) {
				throw serviceException;
			}
			throw new ServiceException("AI 分析執行失敗");
		}
	}

	private FmTaskDetailView authorizedTask(String tenantId, String taskId)
			throws ServiceException {
		if (StringUtils.isAnyBlank(tenantId, taskId)) {
			throw new ServiceException("缺少 Task 資訊");
		}
		return taskRuntimeLogicService.load(tenantId, taskId).getValueEmptyThrowMessage();
	}

	private FmProcessInstance process(String tenantId, String id) throws ServiceException {
		Map<String, Object> values = params("tenantId", tenantId);
		values.put("processInstanceId", id);
		return processInstanceService.selectListByParams(values).getValue().stream()
				.findFirst().orElseThrow(() -> new ServiceException("找不到流程實例"));
	}

	private FmFormData formData(String tenantId, String id) throws ServiceException {
		Map<String, Object> values = params("tenantId", tenantId);
		values.put("formDataId", id);
		return formDataService.selectListByParams(values).getValue().stream()
				.findFirst().orElseThrow(() -> new ServiceException("找不到表單資料"));
	}

	private FmAiProvider provider(String tenantId, String code) throws ServiceException {
		Map<String, Object> values = params("tenantId", tenantId);
		values.put("providerCode", code);
		values.put("status", "ACTIVE");
		return providerService.selectListByParams(values).getValue().stream()
				.filter(p -> StringUtils.isNotBlank(p.getApiKeyContent())).findFirst()
				.orElseThrow(() -> new ServiceException("AI Provider 不存在或未啟用"));
	}

	private FmAiAnalysis createValue(String tenantId, FmTaskDetailView detail,
			FmProcessInstance process, FmFormData data, FmAiProvider provider,
			String actor, String hash, boolean force) {
		FmAiAnalysis value = new FmAiAnalysis();
		value.setTenantId(tenantId);
		value.setAnalysisId(UUID.randomUUID().toString());
		value.setProcessInstanceId(process.getProcessInstanceId());
		value.setTaskId(detail.task().taskId());
		value.setTaskDefKey(detail.task().taskDefKey());
		value.setFormDataId(data.getFormDataId());
		value.setFormRevision(data.getRevisionNo());
		value.setActorAccount(actor);
		value.setProviderCode(provider.getProviderCode());
		value.setProviderType(provider.getProviderType());
		value.setModelId(provider.getModelId());
		value.setConfigVersion(provider.getConfigVersion());
		value.setPromptTemplateVersion(PROMPT_VERSION);
		value.setContentHash(hash);
		value.setForceRefreshFlag(force ? "Y" : "N");
		value.setExecutionStatus("RUNNING");
		value.setStartDate(new Date());
		return value;
	}

	private void startWithRetry(FmAiAnalysis analysis) throws ServiceException {
		for (int attempt = 0; attempt < 3; attempt++) {
			analysis.setGenerationNo(analysisService.nextGenerationNo(
					analysis.getTenantId(), analysis.getContentHash()));
			try {
				analysisService.start(analysis);
				return;
			} catch (RuntimeException exception) {
				if (attempt == 2) {
					throw new ServiceException("AI 分析同時執行，請稍後重試");
				}
			}
		}
	}

	private FmAiProviderConfig config(FmAiProvider provider) throws ServiceException {
		return new FmAiProviderConfig(provider.getProviderCode(), provider.getProviderType(),
				provider.getBaseUrl(), provider.getModelId(),
				apiKeyCipher.decrypt(provider.getApiKeyContent()), provider.getTemperature(),
				provider.getMaxOutputTokens(), provider.getTimeoutSeconds());
	}

	private FmAiAnalysisView view(FmAiAnalysis analysis, boolean cacheHit)
			throws ServiceException {
		try {
			return new FmAiAnalysisView(analysis.getAnalysisId(), analysis.getProviderCode(),
					analysis.getProviderType(), analysis.getModelId(), analysis.getGenerationNo(),
					cacheHit, objectMapper.readTree(analysis.getResultContent()),
					analysis.getInputTokens(), analysis.getOutputTokens(), DISCLAIMER);
		} catch (JacksonException exception) {
			throw new ServiceException("AI 分析結果格式錯誤");
		}
	}

	private void fail(FmAiAnalysis analysis, long startedAt) {
		try {
			analysis.setCompleteDate(new Date());
			analysis.setElapsedMillis(System.currentTimeMillis() - startedAt);
			analysis.setErrorCode("AI_PROVIDER_ERROR");
			analysisService.fail(analysis);
		} catch (Exception ignored) {
			// Preserve the original provider error.
		}
	}

	private void record(String tenantId, String analysisId, String taskId,
			String actor, String type, String status, String errorCode) {
		try {
			accessLedger.record(tenantId, analysisId, taskId, actor, type, status, errorCode);
		} catch (Exception ignored) {
			// Access logging must not conceal the primary result.
		}
	}

	private FmAiProviderOptionView option(FmAiProvider provider) {
		return new FmAiProviderOptionView(provider.getProviderCode(), provider.getDisplayName(),
				provider.getProviderType(), provider.getModelId(),
				"Y".equals(provider.getDefaultFlag()));
	}

	private Map<String, Object> params(String key, Object value) {
		Map<String, Object> result = new HashMap<>();
		result.put(key, value);
		return result;
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
