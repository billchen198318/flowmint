package org.qifu.fm.domain.externalapi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.incident.FmAssignmentIncidentRecorder;
import org.qifu.fm.dto.external.FmExternalRequestStatusRequest;
import org.qifu.fm.dto.external.FmExternalRequestStatusView;
import org.qifu.fm.dto.external.FmExternalRequestSubmitRequest;
import org.qifu.fm.dto.view.FmAssignmentIncidentView;
import org.qifu.fm.entity.FmApiRequest;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskAction;
import org.qifu.fm.service.IFmApiRequestService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTaskActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmExternalRequestStatusService {
	private static final int TIMELINE_LIMIT = 200;
	private final IFmApiRequestService apiRequestService;
	private final IFmProcessInstanceService processInstanceService;
	private final IFmProcessDefService processDefService;
	private final IFmTaskActionService taskActionService;
	private final FmAssignmentIncidentRecorder incidentRecorder;
	private final TaskService taskService;

	public FmExternalRequestStatusService(IFmApiRequestService apiRequestService,
			IFmProcessInstanceService processInstanceService,
			IFmProcessDefService processDefService,
			IFmTaskActionService taskActionService,
			FmAssignmentIncidentRecorder incidentRecorder, TaskService taskService) {
		this.apiRequestService = apiRequestService;
		this.processInstanceService = processInstanceService;
		this.processDefService = processDefService;
		this.taskActionService = taskActionService;
		this.incidentRecorder = incidentRecorder;
		this.taskService = taskService;
	}

	public FmExternalRequestStatusView status(FmExternalRequestStatusRequest request)
			throws ServiceException {
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"runtime.request.read");
		validateLookup(request);
		FmApiRequest ledger = lookup(principal, request);
		if (ledger == null || !"SUCCEEDED".equals(ledger.getRequestStatus())
				|| !principal.allowsProcess(ledger.getProcessDefId())) {
			return null;
		}
		FmProcessInstance process = process(principal.tenantId(),
				ledger.getProcessInstanceId());
		if (process == null) {
			return null;
		}
		List<Task> tasks = taskService.createTaskQuery()
				.processInstanceId(process.getProcessInstanceId()).active()
				.orderByTaskCreateTime().asc().list();
		FmAssignmentIncidentView openIncident = incidentRecorder.findByProcess(
				principal.tenantId(), process.getProcessInstanceId(), "OPEN").stream()
				.findFirst().orElse(null);
		List<FmTaskAction> actions = actions(principal.tenantId(),
				process.getProcessInstanceId());
		String runtimeStatus = process.getInstanceStatus();
		String status = openIncident == null ? runtimeStatus : "INCIDENT";
		boolean includeTimeline = Boolean.TRUE.equals(request.includeTimeline());
		List<FmExternalRequestStatusView.TimelineEntry> timeline = includeTimeline
				? actions.stream().limit(TIMELINE_LIMIT).map(this::timeline).toList()
				: null;
		Date lastChanged = lastChanged(process, actions);
		return new FmExternalRequestStatusView(process.getProcessInstanceId(),
				process.getBusinessKey(), process.getDocumentNumber(), externalReference(ledger),
				process.getProcessDefId(), processName(principal.tenantId(),
						process.getProcessDefId()), process.getProcessVersionNo(),
				ledger.getFormId(), ledger.getFormVersionNo(), process.getInitiatorAccount(),
				ledger.getApplicantAccount(), ledger.getApplicantOrgUnitId(), status,
				runtimeStatus, statusLabel(status), process.getStartDate(), lastChanged,
				process.getEndDate(), tasks.stream().map(this::activeTask).toList(),
				incident(openIncident), timeline,
				includeTimeline && actions.size() > TIMELINE_LIMIT);
	}

	private void validateLookup(FmExternalRequestStatusRequest request)
			throws ServiceException {
		if (request == null) {
			throw new ServiceException("Exactly one status lookup key is required.");
		}
		int count = (StringUtils.isNotBlank(request.processInstanceId()) ? 1 : 0)
				+ (StringUtils.isNotBlank(request.businessKey()) ? 1 : 0)
				+ (StringUtils.isNotBlank(request.documentNumber()) ? 1 : 0)
				+ (request.externalReference() == null ? 0 : 1);
		if (count != 1) {
			throw new ServiceException("Exactly one status lookup key is required.");
		}
		if (request.externalReference() != null && StringUtils.isAnyBlank(
				request.externalReference().sourceSystem(),
				request.externalReference().sourceDocumentType(),
				request.externalReference().sourceDocumentNo())) {
			throw new ServiceException("External reference fields must be provided together.");
		}
	}

	private FmApiRequest lookup(FmExternalApiPrincipal principal,
			FmExternalRequestStatusRequest request) throws ServiceException {
		if (request.externalReference() != null) {
			return apiRequestService.findByExternalReference(principal.tenantId(),
					principal.clientId(), request.externalReference().sourceSystem(),
					request.externalReference().sourceDocumentType(),
					request.externalReference().sourceDocumentNo());
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", principal.tenantId());
		parameters.put("clientId", principal.clientId());
		if (StringUtils.isNotBlank(request.processInstanceId())) {
			parameters.put("processInstanceId", request.processInstanceId().trim());
		} else if (StringUtils.isNotBlank(request.businessKey())) {
			parameters.put("businessKey", request.businessKey().trim());
		} else {
			parameters.put("documentNumber", request.documentNumber().trim());
		}
		List<FmApiRequest> values = apiRequestService.selectListByParams(parameters)
				.getValue();
		return values.size() == 1 ? values.getFirst() : null;
	}

	private FmProcessInstance process(String tenantId, String processInstanceId)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("processInstanceId", processInstanceId);
		List<FmProcessInstance> values = processInstanceService
				.selectListByParams(parameters).getValue();
		return values.size() == 1 ? values.getFirst() : null;
	}

	private List<FmTaskAction> actions(String tenantId, String processInstanceId)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("processInstanceId", processInstanceId);
		return taskActionService.selectListByParams(parameters).getValue().stream()
				.sorted(Comparator.comparing(FmTaskAction::getActionDate,
						Comparator.nullsLast(Date::compareTo)))
				.toList();
	}

	private String processName(String tenantId, String processDefId)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("processDefId", processDefId);
		return processDefService.selectListByParams(parameters).getValue().stream()
				.map(FmProcessDef::getProcessName).findFirst().orElse(null);
	}

	private FmExternalRequestStatusView.ActiveTask activeTask(Task task) {
		String assignmentState = StringUtils.isBlank(task.getAssignee())
				? "CANDIDATE" : "ASSIGNED";
		return new FmExternalRequestStatusView.ActiveTask(task.getTaskDefinitionKey(),
				task.getName(), assignmentState, task.getCreateTime(), task.getDueDate());
	}

	private FmExternalRequestStatusView.Incident incident(
			FmAssignmentIncidentView value) {
		return value == null ? null : new FmExternalRequestStatusView.Incident(
				value.incidentId(), value.errorCode(), value.taskDefKey(),
				value.createdDate(), value.incidentStatus());
	}

	private FmExternalRequestStatusView.TimelineEntry timeline(FmTaskAction action) {
		return new FmExternalRequestStatusView.TimelineEntry(action.getActionType(),
				action.getTaskDefKey(), action.getActionDate(), action.getOutcome());
	}

	private FmExternalRequestSubmitRequest.ExternalReference externalReference(
			FmApiRequest value) {
		return StringUtils.isAnyBlank(value.getSourceSystem(), value.getSourceDocumentType(),
				value.getSourceDocumentNo()) ? null
				: new FmExternalRequestSubmitRequest.ExternalReference(value.getSourceSystem(),
						value.getSourceDocumentType(), value.getSourceDocumentNo());
	}

	private Date lastChanged(FmProcessInstance process, List<FmTaskAction> actions) {
		List<Date> dates = new ArrayList<>();
		dates.add(process.getStartDate());
		dates.add(process.getEndDate());
		dates.add(process.getUdate());
		actions.stream().map(FmTaskAction::getActionDate).forEach(dates::add);
		return dates.stream().filter(java.util.Objects::nonNull).max(Date::compareTo)
				.orElse(process.getStartDate());
	}

	private String statusLabel(String status) {
		return switch (status) {
			case "RUNNING" -> "簽核中";
			case "COMPLETED" -> "已完成";
			case "REJECTED" -> "已駁回";
			case "CANCELLED" -> "已取消";
			case "TERMINATED" -> "已終止";
			case "INCIDENT" -> "流程異常";
			default -> status;
		};
	}
}
