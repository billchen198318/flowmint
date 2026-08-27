package org.qifu.fm.logic.impl;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.dataaction.FmDataActionExecutor;
import org.qifu.fm.domain.dataaction.FmDataActionSqlValidator;
import org.qifu.fm.dto.command.FmDataActionCommand;
import org.qifu.fm.dto.command.FmDataActionStepCommand;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.dto.view.FmDataActionStepView;
import org.qifu.fm.dto.view.FmDataActionView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.entity.FmDataActionStep;
import org.qifu.fm.entity.FmDataActionVersion;
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.logic.IFmDataActionLogicService;
import org.qifu.fm.service.IFmDataActionService;
import org.qifu.fm.service.IFmDataActionStepService;
import org.qifu.fm.service.IFmDataActionVersionService;
import org.qifu.fm.service.IFmDataSourcePoolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmDataActionLogicServiceImpl
		implements IFmDataActionLogicService {

	private static final Set<String> ACTION_TYPES = Set.of(
			"QUERY", "COMMAND", "TRANSACTION");
	private static final Set<String> RESULT_MODES = Set.of(
			"OBJECT", "LIST", "AFFECTED_ROWS", "GENERATED_KEY", "NONE");
	private static final Set<String> RESERVED_PARAMETERS = Set.of(
			"tenantId", "loginAccount", "businessKey",
			"processInstanceId", "now");

	private final IFmDataActionService actionService;
	private final IFmDataActionVersionService versionService;
	private final IFmDataActionStepService stepService;
	private final IFmDataSourcePoolService poolService;
	private final FmDataActionSqlValidator sqlValidator;
	private final FmDataActionExecutor executor;
	private final ObjectMapper objectMapper;

	public FmDataActionLogicServiceImpl(
			IFmDataActionService actionService,
			IFmDataActionVersionService versionService,
			IFmDataActionStepService stepService,
			IFmDataSourcePoolService poolService,
			FmDataActionSqlValidator sqlValidator,
			FmDataActionExecutor executor,
			ObjectMapper objectMapper) {
		this.actionService = actionService;
		this.versionService = versionService;
		this.stepService = stepService;
		this.poolService = poolService;
		this.sqlValidator = sqlValidator;
		this.executor = executor;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmDataActionView> create(FmDataActionCommand command)
			throws ServiceException {
		validateCommand(command);
		validateUnique(command.tenantId(), command.actionCode(), null);
		validatePool(command.tenantId(), command.poolId());

		FmDataAction action = new FmDataAction();
		action.setTenantId(command.tenantId());
		action.setActionId(UUID.randomUUID().toString());
		action.setActionCode(normalizeCode(command.actionCode()));
		action.setCurrentVersionNo(0);
		action.setLockVersion(0);
		applyAction(action, command);
		actionService.insert(action);

		FmDataActionVersion version = newDraft(action, 1);
		versionService.insert(version);
		replaceSteps(action, version, command.steps());
		return load(action.getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmDataActionView> update(FmDataActionCommand command)
			throws ServiceException {
		validateCommand(command);
		FmDataAction action = actionService.selectByPrimaryKey(command.oid())
				.getValueEmptyThrowMessage();
		validateIdentity(action, command);
		validatePool(command.tenantId(), command.poolId());

		FmDataActionVersion draft = findDraft(action.getTenantId(),
				action.getActionId());
		if (draft == null) {
			draft = newDraft(action, action.getCurrentVersionNo() + 1);
			versionService.insert(draft);
		}
		applyAction(action, command);
		if (!actionService.updateOptimistic(action, command.lockVersion())) {
			throw new ServiceException("Data Action 已被其他使用者修改，請重新載入");
		}
		action.setLockVersion(command.lockVersion() + 1);
		replaceSteps(action, draft, command.steps());
		return load(action.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	public DefaultResult<FmDataActionView> load(String oid, String message)
			throws ServiceException {
		FmDataAction action = actionService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		DefaultResult<FmDataActionView> result = success(view(action));
		result.setMessage(message);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmDataActionView> publish(String oid)
			throws ServiceException {
		FmDataAction action = actionService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		FmDataActionVersion draft = requireDraft(action);
		List<FmDataActionStep> steps = loadSteps(action.getTenantId(),
				action.getActionId(), draft.getVersionNo());
		validateForPublish(action, steps);

		draft.setVersionStatus("PUBLISHED");
		draft.setContentSha256(contentDigest(action, steps));
		User currentUser = UserUtils.getCurrentUser();
		draft.setPublishedBy(currentUser == null
				? "SYSTEM" : currentUser.getUsername());
		draft.setPublishedDate(new Date());
		versionService.update(draft);
		action.setCurrentVersionNo(draft.getVersionNo());
		action.setStatus("ACTIVE");
		Integer expectedLockVersion = action.getLockVersion();
		if (!actionService.updateOptimistic(action, expectedLockVersion)) {
			throw new ServiceException("Data Action 已被其他使用者修改，請重新載入");
		}
		action.setLockVersion(expectedLockVersion + 1);
		return load(oid, "Data Action 發布成功");
	}

	@Override
	public DefaultResult<FmDataActionExecutionView> preview(String tenantId,
			String actionId,
			Integer versionNo, Map<String, Object> request, String loginAccount)
			throws ServiceException {
		FmDataAction action = findByActionId(tenantId, actionId);
		FmDataActionVersion version = findVersion(action.getTenantId(),
				actionId, versionNo);
		if (!"DRAFT".equals(version.getVersionStatus())) {
			throw new ServiceException("Preview 只允許草稿版本");
		}
		List<FmDataActionStep> steps = loadSteps(action.getTenantId(),
				actionId, versionNo);
		return success(executor.execute(action, versionNo, steps,
				request, loginAccount, true));
	}

	@Override
	public DefaultResult<FmDataActionExecutionView> execute(String tenantId,
			String actionCode, Integer versionNo, Map<String, Object> request,
			String loginAccount) throws ServiceException {
		FmDataAction action = findPublishedAction(tenantId, actionCode);
		Integer selectedVersion = versionNo == null
				? action.getCurrentVersionNo() : versionNo;
		FmDataActionVersion version = findVersion(tenantId,
				action.getActionId(), selectedVersion);
		if (!"PUBLISHED".equals(version.getVersionStatus())) {
			throw new ServiceException("Data Action Version 尚未發布");
		}
		List<FmDataActionStep> steps = loadSteps(tenantId,
				action.getActionId(), selectedVersion);
		return success(executor.execute(action, selectedVersion, steps,
				request, loginAccount, false));
	}

	@Override
	public void stream(String tenantId, String actionCode, Integer versionNo,
			Map<String, Object> request, String loginAccount, OutputStream outputStream)
			throws ServiceException {
		FmDataAction action = findPublishedAction(tenantId, actionCode);
		Integer selectedVersion = versionNo == null
				? action.getCurrentVersionNo() : versionNo;
		FmDataActionVersion version = findVersion(tenantId,
				action.getActionId(), selectedVersion);
		if (!"PUBLISHED".equals(version.getVersionStatus())) {
			throw new ServiceException("Data Action Version is not published");
		}
		List<FmDataActionStep> steps = loadSteps(tenantId,
				action.getActionId(), selectedVersion);
		executor.stream(action, selectedVersion, steps, request,
				loginAccount, outputStream);
	}

	@Override
	public DefaultResult<List<FmOptionView>> poolOptions(String tenantId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		List<FmOptionView> options = values(poolService.selectListByParams(
				params, "POOL_CODE", "ASC")).stream()
				.map(pool -> new FmOptionView(pool.getPoolId(),
						pool.getPoolCode() + " / " + pool.getPoolName()))
				.toList();
		return success(options);
	}

	@Override
	public DefaultResult<List<FmOptionView>> publishedOptions(String tenantId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		List<FmOptionView> options = values(actionService.selectListByParams(
				params, "ACTION_CODE", "ASC")).stream()
				.filter(action -> action.getCurrentVersionNo() != null
						&& action.getCurrentVersionNo() > 0)
				.map(action -> new FmOptionView(action.getActionCode(),
						action.getActionCode() + " / " + action.getActionName()))
				.toList();
		return success(options);
	}

	@Override
	public DefaultResult<Map<String, Object>> metadata(String tenantId,
			String actionCode) throws ServiceException {
		FmDataAction action = findPublishedAction(tenantId, actionCode);
		Integer versionNo = action.getCurrentVersionNo();
		List<FmDataActionStep> steps = loadSteps(
				tenantId, action.getActionId(), versionNo);
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("actionCode", action.getActionCode());
		metadata.put("actionName", action.getActionName());
		metadata.put("actionType", action.getActionType());
		metadata.put("versionNo", versionNo);
		Set<String> requestFields = readRequestMappings(action.getRequestSchema()).keySet();
		metadata.put("requestFields", new ArrayList<>(requestFields));
		metadata.put("responseKeys", steps.stream()
				.filter(step -> !"NONE".equals(step.getResultMode()))
				.map(FmDataActionStep::getResultKey)
				.toList());
		metadata.put("responseFields", responseFields(steps,
				requestFields.contains("page") && requestFields.contains("pageSize")));
		return success(metadata);
	}

	private List<Map<String, String>> responseFields(List<FmDataActionStep> steps,
			boolean paged) {
		List<Map<String, String>> fields = new ArrayList<>();
		for (FmDataActionStep step : steps) {
			if ("NONE".equals(step.getResultMode())
					|| StringUtils.isBlank(step.getResultKey())) {
				continue;
			}
			String root = step.getResultKey();
			String rootType = switch (step.getResultMode()) {
				case "OBJECT" -> "OBJECT";
				case "LIST" -> "ARRAY_OR_PAGE";
				case "AFFECTED_ROWS", "GENERATED_KEY" -> "OBJECT";
				default -> "ANY";
			};
			fields.add(responseField(root, rootType));
			if (paged && "LIST".equals(step.getResultMode())) {
				fields.add(responseField(root + ".items", "ARRAY"));
				fields.add(responseField(root + ".page", "NUMBER"));
				fields.add(responseField(root + ".pageSize", "NUMBER"));
				fields.add(responseField(root + ".total", "NUMBER"));
				fields.add(responseField(root + ".totalPages", "NUMBER"));
			}
			if ("AFFECTED_ROWS".equals(step.getResultMode())
					|| "GENERATED_KEY".equals(step.getResultMode())) {
				fields.add(responseField(root + ".affectedRows", "NUMBER"));
			}
			if ("GENERATED_KEY".equals(step.getResultMode())) {
				fields.add(responseField(root + ("FOR_EACH".equals(step.getExecutionMode())
						? ".generatedKeys" : ".generatedKey"),
						"FOR_EACH".equals(step.getExecutionMode()) ? "ARRAY" : "NUMBER"));
			}
			if ("FOR_EACH".equals(step.getExecutionMode())
					&& !"GENERATED_KEY".equals(step.getResultMode())) {
				fields.add(responseField(root + ".batchSize", "NUMBER"));
			}
		}
		return fields;
	}

	private Map<String, String> responseField(String path, String type) {
		Map<String, String> field = new HashMap<>();
		field.put("path", path);
		field.put("type", type);
		return field;
	}

	@Override
	public FmDataActionView view(FmDataAction action) throws ServiceException {
		FmDataActionVersion draft = findDraft(action.getTenantId(),
				action.getActionId());
		Integer versionNo = draft == null
				? action.getCurrentVersionNo() : draft.getVersionNo();
		List<FmDataActionStepView> steps = versionNo == null || versionNo == 0
				? List.of()
				: loadSteps(action.getTenantId(), action.getActionId(), versionNo)
						.stream().map(this::stepView).toList();
		return new FmDataActionView(
				action.getOid(),
				action.getTenantId(),
				action.getActionId(),
				action.getActionCode(),
				action.getActionName(),
				action.getPoolId(),
				action.getActionType(),
				action.getRequestSchema(),
				action.getResponseMode(),
				action.getStatus(),
				action.getCurrentVersionNo(),
				draft == null ? null : draft.getVersionNo(),
				draft == null ? null : draft.getVersionStatus(),
				action.getLockVersion(),
				action.getRateLimitPerMinute(),
				action.getDescription(),
				steps);
	}

	private void validateCommand(FmDataActionCommand command)
			throws ServiceException {
		if (command == null
				|| StringUtils.isAnyBlank(command.tenantId(), command.actionCode(),
						command.actionName(), command.poolId(), command.actionType())
				|| !ACTION_TYPES.contains(command.actionType())
				|| command.steps() == null
				|| command.steps().isEmpty()) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		readRequestMappings(command.requestSchema());
	}

	private void validateIdentity(FmDataAction action,
			FmDataActionCommand command) throws ServiceException {
		if (!Objects.equals(action.getTenantId(), command.tenantId())
				|| !action.getActionCode().equalsIgnoreCase(command.actionCode())
				|| !Objects.equals(action.getLockVersion(), command.lockVersion())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private void validateUnique(String tenantId, String actionCode,
			String excludedOid) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		boolean duplicated = values(actionService.selectListByParams(
				params, "ACTION_CODE", "ASC")).stream()
				.filter(action -> !Objects.equals(action.getOid(), excludedOid))
				.anyMatch(action -> action.getActionCode()
						.equalsIgnoreCase(actionCode.trim()));
		if (duplicated) {
			throw new ServiceException("同一 Tenant 的 Action Code 不可重複");
		}
	}

	private void validatePool(String tenantId, String poolId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("poolId", poolId);
		params.put("status", "ACTIVE");
		List<FmDataSourcePool> pools = values(poolService.selectListByParams(
				params, "POOL_ID", "ASC"));
		if (pools.isEmpty()) {
			throw new ServiceException("DataSource Pool 不存在或未啟用");
		}
	}

	private void applyAction(FmDataAction action,
			FmDataActionCommand command) {
		action.setActionName(command.actionName().trim());
		action.setPoolId(command.poolId());
		action.setActionType(command.actionType());
		action.setRequestSchema(StringUtils.defaultIfBlank(
				command.requestSchema(), "{}"));
		action.setResponseMode(StringUtils.defaultIfBlank(
				command.responseMode(), "COMPOSITE"));
		action.setStatus(StringUtils.defaultIfBlank(command.status(), "DRAFT"));
		if (command.rateLimitPerMinute() != null
				&& (command.rateLimitPerMinute() < 1
						|| command.rateLimitPerMinute() > 100000)) {
			throw new IllegalArgumentException("Rate Limit 必須介於 1 至 100000");
		}
		action.setRateLimitPerMinute(command.rateLimitPerMinute());
		action.setDescription(StringUtils.trimToNull(command.description()));
	}

	private FmDataActionVersion newDraft(FmDataAction action, int versionNo) {
		FmDataActionVersion version = new FmDataActionVersion();
		version.setTenantId(action.getTenantId());
		version.setActionId(action.getActionId());
		version.setVersionNo(versionNo);
		version.setVersionStatus("DRAFT");
		return version;
	}

	private void replaceSteps(FmDataAction action, FmDataActionVersion version,
			List<FmDataActionStepCommand> commands) throws ServiceException {
		for (FmDataActionStep existing : loadSteps(action.getTenantId(),
				action.getActionId(), version.getVersionNo())) {
			stepService.delete(existing);
		}
		Set<String> stepCodes = new LinkedHashSet<>();
		Set<String> resultKeys = new LinkedHashSet<>();
		for (FmDataActionStepCommand command : commands) {
			FmDataActionStep step = toStep(action, version, command);
			if (!stepCodes.add(step.getStepCode())
					|| !resultKeys.add(step.getResultKey())) {
				throw new ServiceException("Step Code 與 Result Key 不可重複");
			}
			stepService.insert(step);
		}
	}

	private FmDataActionStep toStep(FmDataAction action,
			FmDataActionVersion version, FmDataActionStepCommand command)
			throws ServiceException {
		if (command == null
				|| StringUtils.isAnyBlank(command.stepCode(), command.stepName(),
						command.statementType(), command.sqlContent(),
						command.resultKey(), command.resultMode())
				|| !RESULT_MODES.contains(command.resultMode())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		FmDataActionStep step = new FmDataActionStep();
		step.setTenantId(action.getTenantId());
		step.setActionId(action.getActionId());
		step.setVersionNo(version.getVersionNo());
		step.setStepId(UUID.randomUUID().toString());
		step.setStepCode(normalizeCode(command.stepCode()));
		step.setStepName(command.stepName().trim());
		step.setExecutionOrder(Objects.requireNonNullElse(
				command.executionOrder(), 10));
		step.setStatementType(command.statementType());
		step.setExecutionMode(StringUtils.defaultIfBlank(
				command.executionMode(), "ONCE"));
		step.setSqlContent(command.sqlContent().trim());
		step.setArrayPath(StringUtils.trimToNull(command.arrayPath()));
		step.setResultKey(command.resultKey().trim());
		step.setResultMode(command.resultMode());
		step.setExpectAffectedRows(command.expectAffectedRows());
		step.setContinueCondition(StringUtils.trimToNull(
				command.continueCondition()));
		step.setQueryTimeoutSeconds(Objects.requireNonNullElse(
				command.queryTimeoutSeconds(), 30));
		step.setMaxRows(Objects.requireNonNullElse(command.maxRows(), 1000));
		step.setRetryCount(Objects.requireNonNullElse(command.retryCount(), 0));
		step.setRetryDelayMillis(Objects.requireNonNullElse(command.retryDelayMillis(), 0));
		if (step.getRetryCount() < 0 || step.getRetryCount() > 5
				|| step.getRetryDelayMillis() < 0 || step.getRetryDelayMillis() > 5000) {
			throw new ServiceException("Step 重試次數必須為 0～5，間隔必須為 0～5000ms");
		}
		if (step.getRetryCount() > 0
				&& (!"QUERY".equals(action.getActionType())
						|| !Set.of("SELECT_ONE", "SELECT_LIST")
								.contains(step.getStatementType()))) {
			throw new ServiceException("第一版重試政策只允許 QUERY Action 的 SELECT Step");
		}
		step.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		return step;
	}

	private void validateForPublish(FmDataAction action,
			List<FmDataActionStep> steps) throws ServiceException {
		validatePool(action.getTenantId(), action.getPoolId());
		Set<String> parameters = new LinkedHashSet<>(RESERVED_PARAMETERS);
		parameters.addAll(readRequestMappings(action.getRequestSchema()).keySet());
		if (steps.isEmpty()) {
			throw new ServiceException("Data Action 至少需要一個 SQL Step");
		}
		for (FmDataActionStep step : steps) {
			sqlValidator.validate(step, action.getActionType(), parameters);
		}
	}

	private Map<String, String> readRequestMappings(String requestSchema)
			throws ServiceException {
		try {
			return objectMapper.readValue(StringUtils.defaultIfBlank(
					requestSchema, "{}"),
					new TypeReference<Map<String, String>>() { });
		} catch (Exception exception) {
			throw new ServiceException("Request Schema 必須是 JSON Path Mapping 物件");
		}
	}

	private FmDataActionVersion requireDraft(FmDataAction action)
			throws ServiceException {
		FmDataActionVersion draft = findDraft(action.getTenantId(),
				action.getActionId());
		if (draft == null) {
			throw new ServiceException("目前沒有可發布的草稿版本");
		}
		return draft;
	}

	private FmDataActionVersion findDraft(String tenantId, String actionId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("actionId", actionId);
		params.put("versionStatus", "DRAFT");
		return values(versionService.selectListByParams(
				params, "VERSION_NO", "DESC")).stream()
				.findFirst().orElse(null);
	}

	private FmDataActionVersion findVersion(String tenantId, String actionId,
			Integer versionNo) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("actionId", actionId);
		params.put("versionNo", versionNo);
		return values(versionService.selectListByParams(
				params, "VERSION_NO", "ASC")).stream()
				.findFirst().orElseThrow(() ->
						new ServiceException("找不到 Data Action Version"));
	}

	private FmDataAction findByActionId(String tenantId, String actionId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("actionId", actionId);
		return values(actionService.selectListByParams(
				params, "ACTION_ID", "ASC")).stream()
				.findFirst().orElseThrow(() ->
						new ServiceException("找不到 Data Action"));
	}

	private FmDataAction findPublishedAction(String tenantId, String actionCode)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("actionCode", normalizeCode(actionCode));
		params.put("status", "ACTIVE");
		return values(actionService.selectListByParams(
				params, "ACTION_CODE", "ASC")).stream()
				.findFirst().orElseThrow(() ->
						new ServiceException("找不到已發布的 Data Action"));
	}

	private List<FmDataActionStep> loadSteps(String tenantId, String actionId,
			Integer versionNo) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("actionId", actionId);
		params.put("versionNo", versionNo);
		return new ArrayList<>(values(stepService.selectListByParams(
				params, "EXECUTION_ORDER", "ASC")));
	}

	private FmDataActionStepView stepView(FmDataActionStep step) {
		return new FmDataActionStepView(
				step.getOid(),
				step.getStepId(),
				step.getStepCode(),
				step.getStepName(),
				step.getExecutionOrder(),
				step.getStatementType(),
				step.getExecutionMode(),
				step.getSqlContent(),
				step.getArrayPath(),
				step.getResultKey(),
				step.getResultMode(),
				step.getExpectAffectedRows(),
				step.getContinueCondition(),
				step.getQueryTimeoutSeconds(),
				step.getMaxRows(),
				step.getRetryCount(),
				step.getRetryDelayMillis(),
				step.getStatus());
	}

	private String contentDigest(FmDataAction action,
			List<FmDataActionStep> steps) throws ServiceException {
		StringBuilder content = new StringBuilder()
				.append(action.getActionType()).append('\n')
				.append(action.getRequestSchema()).append('\n');
		steps.forEach(step -> content
				.append(step.getExecutionOrder()).append('|')
				.append(step.getStepCode()).append('|')
				.append(step.getStatementType()).append('|')
				.append(step.getSqlContent()).append('\n'));
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(
					content.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new ServiceException("無法建立 Data Action 內容摘要");
		}
	}

	private String normalizeCode(String code) {
		return code == null ? "" : code.trim().toUpperCase();
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}

	private <T> List<T> values(DefaultResult<List<T>> result) {
		return Objects.requireNonNullElse(result.getValue(), List.of());
	}
}
