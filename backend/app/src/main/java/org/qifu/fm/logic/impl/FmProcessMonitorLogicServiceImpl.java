package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.command.FmProcessMonitorRequest;
import org.qifu.fm.dto.view.FmProcessMonitorView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.logic.IFmProcessMonitorLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmProcessMonitorLogicServiceImpl implements IFmProcessMonitorLogicService {

	private static final List<String> STATUSES = List.of(
			"RUNNING", "COMPLETED", "REJECTED", "CANCELLED", "TERMINATED");

	private final TaskService taskService;
	private final IFmProcessInstanceService processInstanceService;
	private final IFmProcessDefService processDefService;
	private final IFmFormDataService formDataService;

	public FmProcessMonitorLogicServiceImpl(
			TaskService taskService,
			IFmProcessInstanceService processInstanceService,
			IFmProcessDefService processDefService,
			IFmFormDataService formDataService) {
		this.taskService = taskService;
		this.processInstanceService = processInstanceService;
		this.processDefService = processDefService;
		this.formDataService = formDataService;
	}

	@Override
	public DefaultResult<List<FmProcessMonitorView>> find(
			String tenantId, FmProcessMonitorRequest request) throws ServiceException {
		requireOperator();
		String status = request == null ? null : StringUtils.trimToNull(request.status());
		String keyword = request == null ? null : StringUtils.trimToNull(request.keyword());
		if (StringUtils.isBlank(tenantId) || (status != null && !STATUSES.contains(status))) {
			throw new ServiceException("流程監控查詢條件不正確");
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("instanceStatus", status);
		List<FmProcessMonitorView> values = new ArrayList<>();
		for (FmProcessInstance process : processInstanceService.selectListByParams(
				parameters, "START_DATE", "DESC").getValue()) {
			FmFormData formData = formData(tenantId, process.getFormDataId());
			FmProcessDef processDef = processDef(tenantId, process.getProcessDefId());
			FmProcessMonitorView view = view(process, processDef, formData);
			if (keyword == null || matches(view, keyword)) {
				values.add(view);
			}
		}
		return success(List.copyOf(values));
	}

	private FmProcessMonitorView view(FmProcessInstance process,
			FmProcessDef processDef, FmFormData formData) {
		List<String> tasks = "RUNNING".equals(process.getInstanceStatus())
				? taskService.createTaskQuery()
						.processInstanceId(process.getProcessInstanceId()).list().stream()
						.map(value -> value.getName()).distinct().toList()
				: List.of();
		return new FmProcessMonitorView(process.getProcessInstanceId(),
				process.getBusinessKey(), processDef.getProcessName(),
				process.getProcessVersionNo(), formData.getOwnerAccount(),
				process.getInitiatorAccount(), process.getInstanceStatus(), tasks,
				process.getStartDate(), process.getEndDate());
	}

	private boolean matches(FmProcessMonitorView value, String keyword) {
		String normalized = keyword.toLowerCase(java.util.Locale.ROOT);
		return List.of(value.processInstanceId(), value.businessKey(), value.processName(),
				value.ownerAccount(), value.initiatorAccount()).stream()
				.filter(java.util.Objects::nonNull)
				.anyMatch(item -> item.toLowerCase(java.util.Locale.ROOT).contains(normalized));
	}

	private FmFormData formData(String tenantId, String formDataId)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("formDataId", formDataId);
		return formDataService.selectListByParams(parameters).getValue().stream()
				.findFirst().orElseThrow(() -> new ServiceException("流程表單資料不存在"));
	}

	private FmProcessDef processDef(String tenantId, String processDefId)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("processDefId", processDefId);
		return processDefService.selectListByParams(parameters).getValue().stream()
				.findFirst().orElseThrow(() -> new ServiceException("流程定義不存在"));
	}

	private void requireOperator() throws ServiceException {
		if (!UserUtils.isAdmin() && !UserUtils.hasRole("FLOWMINT_OPERATIONS")) {
			throw new ServiceException("需要流程營運管理權限");
		}
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
