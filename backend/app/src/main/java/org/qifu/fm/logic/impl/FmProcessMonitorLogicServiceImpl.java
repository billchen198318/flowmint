package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.PageOf;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.command.FmProcessMonitorRequest;
import org.qifu.fm.dto.command.FmOperationsReportRequest;
import org.qifu.fm.dto.view.FmProcessMonitorView;
import org.qifu.fm.dto.view.FmProcessMonitorDetailView;
import org.qifu.fm.dto.view.FmProcessMonitorPageView;
import org.qifu.fm.dto.view.FmOperationsReportView;
import org.qifu.fm.dto.view.FmOperationsDailyReportView;
import org.qifu.fm.dto.view.FmFormSnapshotView;
import org.qifu.fm.dto.view.FmTaskActionView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskAction;
import org.qifu.fm.logic.IFmProcessMonitorLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTaskActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmProcessMonitorLogicServiceImpl implements IFmProcessMonitorLogicService {

	private static final List<String> STATUSES = List.of(
			"RUNNING", "COMPLETED", "REJECTED", "CANCELLED", "TERMINATED");

	private final TaskService taskService;
	private final IFmProcessInstanceService processInstanceService;
	private final IFmProcessDefService processDefService;
	private final IFmFormDataService formDataService;
	private final IFmTaskActionService taskActionService;
	private final IFmFormSnapshotService formSnapshotService;
	private final ObjectMapper objectMapper;

	public FmProcessMonitorLogicServiceImpl(
			TaskService taskService,
			IFmProcessInstanceService processInstanceService,
			IFmProcessDefService processDefService,
			IFmFormDataService formDataService,
			IFmTaskActionService taskActionService,
			IFmFormSnapshotService formSnapshotService,
			ObjectMapper objectMapper) {
		this.taskService = taskService;
		this.processInstanceService = processInstanceService;
		this.processDefService = processDefService;
		this.formDataService = formDataService;
		this.taskActionService = taskActionService;
		this.formSnapshotService = formSnapshotService;
		this.objectMapper = objectMapper;
	}

	@Override
	public DefaultResult<FmOperationsReportView> report(
			String tenantId, FmOperationsReportRequest request) throws ServiceException {
		requireOperator();
		if (StringUtils.isBlank(tenantId)) {
			throw new ServiceException("Tenant is required");
		}
		java.time.LocalDate endDay = parseDate(
				request == null ? null : request.endDate(), java.time.LocalDate.now());
		java.time.LocalDate startDay = parseDate(
				request == null ? null : request.startDate(), endDay.minusDays(29));
		if (startDay.isAfter(endDay) || startDay.isBefore(endDay.minusDays(365))) {
			throw new ServiceException("報表日期區間必須介於 1 至 366 天");
		}
		java.time.ZoneId zone = java.time.ZoneId.systemDefault();
		Date startDate = Date.from(startDay.atStartOfDay(zone).toInstant());
		Date endExclusive = Date.from(endDay.plusDays(1).atStartOfDay(zone).toInstant());
		var summary = processInstanceService.operationsSummary(
				tenantId, startDate, endExclusive);
		Map<String, org.qifu.fm.model.FmOperationsDailySummary> dailyByDate =
				processInstanceService.operationsDailySummary(tenantId, startDate, endExclusive)
						.stream().collect(java.util.stream.Collectors.toMap(
								org.qifu.fm.model.FmOperationsDailySummary::getReportDate,
								value -> value));
		List<FmOperationsDailyReportView> dailyTrend = startDay.datesUntil(
				endDay.plusDays(1)).map(day -> {
			var value = dailyByDate.get(day.toString());
			return new FmOperationsDailyReportView(day.toString(),
					value == null ? 0L : number(value.getStartedProcesses()),
					value == null ? 0L : number(value.getCompletedProcesses()),
					value == null ? 0L : number(value.getAverageCompletedMinutes()));
		}).toList();
		Date now = new Date();
		long overdue = taskService.createTaskQuery()
				.processVariableValueEquals(
						org.qifu.fm.flowable.FmTaskAssignmentListener.VARIABLE_TENANT_ID,
						tenantId)
				.taskDueBefore(now).count();
		long dueSoon = taskService.createTaskQuery()
				.processVariableValueEquals(
						org.qifu.fm.flowable.FmTaskAssignmentListener.VARIABLE_TENANT_ID,
						tenantId)
				.taskDueAfter(now)
				.taskDueBefore(Date.from(now.toInstant().plus(24,
						java.time.temporal.ChronoUnit.HOURS))).count();
		return success(new FmOperationsReportView(startDate,
				Date.from(endDay.atTime(23, 59, 59).atZone(zone).toInstant()),
				number(summary.getTotalProcesses()), number(summary.getRunningProcesses()),
				number(summary.getCompletedProcesses()), number(summary.getRejectedProcesses()),
				number(summary.getCancelledProcesses()), number(summary.getTerminatedProcesses()),
				number(summary.getAverageCompletedMinutes()), overdue, dueSoon, dailyTrend));
	}

	private java.time.LocalDate parseDate(String value, java.time.LocalDate defaultValue)
			throws ServiceException {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		try {
			return java.time.LocalDate.parse(value);
		} catch (java.time.format.DateTimeParseException exception) {
			throw new ServiceException("報表日期格式必須為 yyyy-MM-dd");
		}
	}

	private long number(Long value) {
		return value == null ? 0L : value;
	}

	@Override
	public DefaultResult<FmProcessMonitorDetailView> load(
			String tenantId, String processInstanceId) throws ServiceException {
		requireOperator();
		if (StringUtils.isAnyBlank(tenantId, processInstanceId)) {
			throw new ServiceException("Tenant and process instance are required");
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("processInstanceId", processInstanceId);
		FmProcessInstance process = processInstanceService.selectListByParams(parameters)
				.getValue().stream().findFirst()
				.orElseThrow(() -> new ServiceException("Process instance not found"));
		FmProcessMonitorView processView = view(process,
				processDef(tenantId, process.getProcessDefId()),
				formData(tenantId, process.getFormDataId()));
		List<FmTaskActionView> actions = taskActionService
				.selectListByParams(parameters, "ACTION_DATE", "ASC").getValue().stream()
				.map(this::actionView).toList();
		List<FmFormSnapshotView> snapshots = formSnapshotService
				.selectListByParams(parameters, "SNAPSHOT_DATE", "ASC").getValue().stream()
				.map(this::snapshotView).toList();
		return success(new FmProcessMonitorDetailView(processView, actions, snapshots));
	}

	private FmTaskActionView actionView(FmTaskAction action) {
		return new FmTaskActionView(action.getActionType(), action.getOutcome(),
				action.getActorAccount(), action.getCommentText(), action.getReason(),
				action.getActionDate());
	}

	private FmFormSnapshotView snapshotView(FmFormSnapshot snapshot) {
		return new FmFormSnapshotView(snapshot.getFormSnapshotId(), snapshot.getTaskId(),
				snapshot.getActionType(), snapshot.getRevisionNo(), snapshot.getContentSha256(),
				snapshot.getSnapshotDate(), parseData(snapshot.getDataContent()));
	}

	private Map<String, Object> parseData(String content) {
		if (StringUtils.isBlank(content)) {
			return Map.of();
		}
		try {
			return objectMapper.readValue(content,
					new TypeReference<Map<String, Object>>() { });
		} catch (Exception exception) {
			throw new IllegalStateException("Invalid form snapshot JSON", exception);
		}
	}

	@Override
	public DefaultResult<FmProcessMonitorPageView> find(
			String tenantId, FmProcessMonitorRequest request) throws ServiceException {
		requireOperator();
		String status = request == null ? null : StringUtils.trimToNull(request.status());
		String keyword = request == null ? null : StringUtils.trimToNull(request.keyword());
		int page = request == null || request.page() == null ? 1 : request.page();
		int pageSize = request == null || request.pageSize() == null ? 30 : request.pageSize();
		if (StringUtils.isBlank(tenantId) || (status != null && !STATUSES.contains(status))
				|| page < 1 || !List.of(10, 30, 50, 100).contains(pageSize)) {
			throw new ServiceException("流程監控查詢條件不正確");
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("instanceStatus", status);
		parameters.put("keywordLike", keyword == null ? null : escapedLike(keyword));
		PageOf pageOf = new PageOf();
		pageOf.setSelect(String.valueOf(page));
		pageOf.setShowRow(String.valueOf(pageSize));
		pageOf.orderBy("START_DATE").sortTypeDesc();
		QueryResult<List<FmProcessInstance>> query = processInstanceService
				.findPage(parameters, pageOf);
		List<FmProcessMonitorView> values = new ArrayList<>();
		for (FmProcessInstance process : query.getValue()) {
			FmFormData formData = formData(tenantId, process.getFormDataId());
			FmProcessDef processDef = processDef(tenantId, process.getProcessDefId());
			values.add(view(process, processDef, formData));
		}
		PageOf resultPage = query.getPageOf();
		return success(new FmProcessMonitorPageView(List.copyOf(values),
				resultPage.getLongValue(resultPage.getCountSize()),
				resultPage.getIntegerValue(resultPage.getSize()),
				resultPage.getIntegerValue(resultPage.getSelect()),
				resultPage.getIntegerValue(resultPage.getShowRow())));
	}

	private String escapedLike(String keyword) {
		return "%" + keyword.replace("\\", "\\\\")
				.replace("%", "\\%").replace("_", "\\_") + "%";
	}

	private FmProcessMonitorView view(FmProcessInstance process,
			FmProcessDef processDef, FmFormData formData) {
		List<Task> activeTasks = "RUNNING".equals(process.getInstanceStatus())
				? taskService.createTaskQuery()
						.processInstanceId(process.getProcessInstanceId()).list()
				: List.of();
		List<String> taskNames = activeTasks.stream().map(Task::getName).distinct().toList();
		Date now = new Date();
		Date nearestDueDate = activeTasks.stream().map(Task::getDueDate)
				.filter(java.util.Objects::nonNull).min(Date::compareTo).orElse(null);
		int overdueTaskCount = (int) activeTasks.stream().map(Task::getDueDate)
				.filter(java.util.Objects::nonNull).filter(value -> !value.after(now)).count();
		Date elapsedEnd = process.getEndDate() == null ? now : process.getEndDate();
		long elapsedMinutes = Math.max(0L,
				(elapsedEnd.getTime() - process.getStartDate().getTime()) / 60000L);
		return new FmProcessMonitorView(process.getProcessInstanceId(),
				process.getBusinessKey(), processDef.getProcessName(),
				process.getProcessVersionNo(), formData.getOwnerAccount(),
				process.getInitiatorAccount(), process.getInstanceStatus(), taskNames,
				nearestDueDate, overdueTaskCount, elapsedMinutes,
				process.getStartDate(), process.getEndDate());
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
