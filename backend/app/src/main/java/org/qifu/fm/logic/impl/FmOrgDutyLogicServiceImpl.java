package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.dto.command.FmEmployeeDutyCommand;
import org.qifu.fm.dto.command.FmOrgDutyCommand;
import org.qifu.fm.dto.view.FmEmployeeDutyView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgDutyView;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeDuty;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgDuty;
import org.qifu.fm.logic.IFmOrgDutyLogicService;
import org.qifu.fm.service.IFmEmployeeDutyService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgDutyService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmOrgDutyLogicServiceImpl implements IFmOrgDutyLogicService {

	private static final List<String> DUTY_TYPES = List.of("APPROVAL", "REVIEW", "NOTIFY");

	private final IFmOrgDutyService dutyService;
	private final IFmEmployeeDutyService employeeDutyService;
	private final IFmEmployeeOrgAssignmentService assignmentService;
	private final IFmEmployeeService employeeService;
	private final IFmOrgUnitVersionService orgUnitVersionService;
	private final IFmTenantService tenantService;
	private final FmTenantAccessGuard tenantAccessGuard;

	public FmOrgDutyLogicServiceImpl(
			IFmOrgDutyService dutyService,
			IFmEmployeeDutyService employeeDutyService,
			IFmEmployeeOrgAssignmentService assignmentService,
			IFmEmployeeService employeeService,
			IFmOrgUnitVersionService orgUnitVersionService,
			IFmTenantService tenantService, FmTenantAccessGuard tenantAccessGuard) {
		this.dutyService = dutyService;
		this.employeeDutyService = employeeDutyService;
		this.assignmentService = assignmentService;
		this.employeeService = employeeService;
		this.orgUnitVersionService = orgUnitVersionService;
		this.tenantService = tenantService;
		this.tenantAccessGuard = tenantAccessGuard;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgDutyView> create(FmOrgDutyCommand command) throws ServiceException {
		validateDuty(command);
		tenantAccessGuard.requireAccess(command.tenantId());
		FmOrgDuty duty = new FmOrgDuty();
		duty.setTenantId(command.tenantId());
		duty.setDutyId(UUID.randomUUID().toString());
		duty.setDutyCode(command.dutyCode());
		applyDuty(duty, command);
		dutyService.insert(duty);
		return load(duty.getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmOrgDutyView> load(String oid, String message) throws ServiceException {
		FmOrgDuty duty = dutyService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(duty.getTenantId());
		DefaultResult<FmOrgDutyView> result = success(view(duty));
		result.setMessage(message);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgDutyView> update(FmOrgDutyCommand command) throws ServiceException {
		FmOrgDuty duty = dutyService.selectByPrimaryKey(command.oid()).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(duty.getTenantId());
		if (!duty.getTenantId().equals(command.tenantId())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		validateDuty(command);
		if (!duty.getOrgUnitId().equals(command.orgUnitId())) {
			Map<String, Object> params = new HashMap<>();
			params.put("tenantId", duty.getTenantId());
			params.put("dutyId", duty.getDutyId());
			if (!employeeDutyService.selectListByParams(params).getValue().isEmpty()) {
				throw new ServiceException("職務已有擔任人紀錄，請另建職務以更換部門");
			}
		}
		applyDuty(duty, command);
		dutyService.update(duty);
		return load(duty.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgDutyView> deactivate(String oid) throws ServiceException {
		FmOrgDuty duty = dutyService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(duty.getTenantId());
		duty.setStatus("INACTIVE");
		Date now = new Date();
		if (!duty.getEffectiveFrom().after(now)
				&& (duty.getEffectiveTo() == null || duty.getEffectiveTo().after(now))) {
			duty.setEffectiveTo(now);
		}
		dutyService.update(duty);
		return load(oid, BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgDutyView> saveAssignee(FmEmployeeDutyCommand command)
			throws ServiceException {
		FmOrgDuty duty = dutyService.selectByPrimaryKey(command.dutyOid()).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(duty.getTenantId());
		FmEmployeeOrgAssignment assignment = requiredAssignment(duty, command.employeeOrgAssignmentId());
		validatePeriod(command.effectiveFrom(), command.effectiveTo(), "擔任期間不正確");
		validatePrimary(command, duty);

		FmEmployeeDuty employeeDuty;
		if (StringUtils.isBlank(command.oid())) {
			employeeDuty = new FmEmployeeDuty();
			employeeDuty.setTenantId(duty.getTenantId());
			employeeDuty.setDutyId(duty.getDutyId());
			employeeDuty.setEmployeeDutyId(UUID.randomUUID().toString());
		} else {
			employeeDuty = employeeDutyService.selectByPrimaryKey(command.oid())
					.getValueEmptyThrowMessage();
			if (!duty.getTenantId().equals(employeeDuty.getTenantId())
					|| !duty.getDutyId().equals(employeeDuty.getDutyId())) {
				throw new ServiceException(BaseSystemMessage.parameterIncorrect());
			}
		}
		employeeDuty.setEmployeeOrgAssignmentId(assignment.getEmployeeOrgAssignmentId());
		employeeDuty.setIsPrimary(StringUtils.defaultIfBlank(command.isPrimary(), "N"));
		employeeDuty.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		employeeDuty.setEffectiveFrom(command.effectiveFrom());
		employeeDuty.setEffectiveTo(command.effectiveTo());
		if (StringUtils.isBlank(command.oid())) {
			employeeDutyService.insert(employeeDuty);
		} else {
			employeeDutyService.update(employeeDuty);
		}
		return load(duty.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgDutyView> deactivateAssignee(String dutyOid, String oid)
			throws ServiceException {
		FmOrgDuty duty = dutyService.selectByPrimaryKey(dutyOid).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(duty.getTenantId());
		FmEmployeeDuty employeeDuty = employeeDutyService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		if (!duty.getTenantId().equals(employeeDuty.getTenantId())
				|| !duty.getDutyId().equals(employeeDuty.getDutyId())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		employeeDuty.setStatus("INACTIVE");
		Date now = new Date();
		if (!employeeDuty.getEffectiveFrom().after(now)
				&& (employeeDuty.getEffectiveTo() == null || employeeDuty.getEffectiveTo().after(now))) {
			employeeDuty.setEffectiveTo(now);
		}
		employeeDutyService.update(employeeDuty);
		return load(dutyOid, BaseSystemMessage.updateSuccess());
	}

	@Override
	public FmOrgDutyView view(FmOrgDuty duty) throws ServiceException {
		tenantAccessGuard.requireAccess(duty.getTenantId());
		Map<String, String> unitLabels = orgUnitOptions(duty.getTenantId()).getValue().stream()
				.collect(Collectors.toMap(FmOptionView::value, FmOptionView::label));
		Map<String, String> assignmentLabels = assignmentLabelMap(duty);
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", duty.getTenantId());
		params.put("dutyId", duty.getDutyId());
		List<FmEmployeeDutyView> assignees = employeeDutyService
				.selectListByParams(params, "IS_PRIMARY DESC,EFFECTIVE_FROM", "ASC").getValue().stream()
				.map(value -> new FmEmployeeDutyView(
						value.getOid(),
						value.getEmployeeDutyId(),
						value.getEmployeeOrgAssignmentId(),
						assignmentLabels.getOrDefault(value.getEmployeeOrgAssignmentId(),
								value.getEmployeeOrgAssignmentId()),
						value.getIsPrimary(),
						value.getStatus(),
						value.getEffectiveFrom(),
						value.getEffectiveTo()))
				.toList();
		return new FmOrgDutyView(
				duty.getOid(), duty.getTenantId(), duty.getDutyId(), duty.getOrgUnitId(),
				unitLabels.getOrDefault(duty.getOrgUnitId(), duty.getOrgUnitId()),
				duty.getDutyCode(), duty.getDutyName(), duty.getDutyType(), duty.getStatus(),
				duty.getEffectiveFrom(), duty.getEffectiveTo(), duty.getDescription(), assignees);
	}

	@Override
	public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
		var readableTenantIds = tenantAccessGuard.readableTenantIds();
		Map<String, Object> params = new HashMap<>();
		params.put("status", "ACTIVE");
		return success(tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue().stream()
				.filter(value -> readableTenantIds.contains(value.getTenantId()))
				.map(value -> new FmOptionView(value.getTenantId(),
						value.getTenantCode() + "／" + value.getTenantName()))
				.toList());
	}

	@Override
	public DefaultResult<List<FmOptionView>> orgUnitOptions(String tenantId) throws ServiceException {
		if (StringUtils.isBlank(tenantId)) {
			return success(List.of());
		}
		tenantAccessGuard.requireAccess(tenantId);
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		return success(orgUnitVersionService.selectCurrentTree(params).getValue().stream()
				.map(value -> new FmOptionView(value.getOrgUnitId(),
						value.getUnitCode() + "／" + value.getUnitName()))
				.toList());
	}

	@Override
	public DefaultResult<List<FmOptionView>> assignmentOptions(String dutyOid) throws ServiceException {
		FmOrgDuty duty = dutyService.selectByPrimaryKey(dutyOid).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(duty.getTenantId());
		return success(assignmentLabelMap(duty).entrySet().stream()
				.map(entry -> new FmOptionView(entry.getKey(), entry.getValue()))
				.sorted((left, right) -> left.label().compareTo(right.label()))
				.toList());
	}

	private void validateDuty(FmOrgDutyCommand command) throws ServiceException {
		if (StringUtils.isAnyBlank(command.tenantId(), command.orgUnitId(), command.dutyCode(),
				command.dutyName(), command.dutyType()) || !DUTY_TYPES.contains(command.dutyType())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		validatePeriod(command.effectiveFrom(), command.effectiveTo(), "職務有效期間不正確");
		if (orgUnitOptions(command.tenantId()).getValue().stream()
				.noneMatch(value -> value.value().equals(command.orgUnitId()))) {
			throw new ServiceException("部門不存在、已停用或不屬於所選 Tenant");
		}
	}

	private FmEmployeeOrgAssignment requiredAssignment(FmOrgDuty duty, String assignmentId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", duty.getTenantId());
		params.put("orgUnitId", duty.getOrgUnitId());
		params.put("status", "ACTIVE");
		return assignmentService.selectListByParams(params, "EFFECTIVE_FROM", "ASC").getValue().stream()
				.filter(this::currentlyEffective)
				.filter(value -> value.getEmployeeOrgAssignmentId().equals(assignmentId))
				.findFirst()
				.orElseThrow(() -> new ServiceException("所選員工沒有此部門的有效任職"));
	}

	private void validatePrimary(FmEmployeeDutyCommand command, FmOrgDuty duty)
			throws ServiceException {
		if (!"Y".equals(StringUtils.defaultIfBlank(command.isPrimary(), "N"))
				|| !"ACTIVE".equals(StringUtils.defaultIfBlank(command.status(), "ACTIVE"))) {
			return;
		}
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", duty.getTenantId());
		params.put("dutyId", duty.getDutyId());
		params.put("status", "ACTIVE");
		boolean duplicate = employeeDutyService.selectListByParams(params, "EFFECTIVE_FROM", "ASC")
				.getValue().stream()
				.anyMatch(value -> "Y".equals(value.getIsPrimary())
						&& !value.getOid().equals(command.oid()));
		if (duplicate) {
			throw new ServiceException("同一職務只能有一位有效的主要擔任人");
		}
	}

	private Map<String, String> assignmentLabelMap(FmOrgDuty duty) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", duty.getTenantId());
		params.put("orgUnitId", duty.getOrgUnitId());
		params.put("status", "ACTIVE");
		List<FmEmployeeOrgAssignment> assignments = assignmentService
				.selectListByParams(params, "IS_PRIMARY DESC,EFFECTIVE_FROM", "ASC").getValue();
		Map<String, FmEmployee> employees = employeeService
				.selectListByParams(Map.of("tenantId", duty.getTenantId(), "status", "ACTIVE"),
						"EMPLOYEE_NO", "ASC").getValue().stream()
				.collect(Collectors.toMap(FmEmployee::getEmployeeId, Function.identity()));
		Map<String, String> labels = new HashMap<>();
		for (FmEmployeeOrgAssignment assignment : assignments) {
			if (!currentlyEffective(assignment)) {
				continue;
			}
			FmEmployee employee = employees.get(assignment.getEmployeeId());
			if (employee != null) {
				labels.put(assignment.getEmployeeOrgAssignmentId(),
						employee.getEmployeeNo() + "／" + employee.getDisplayName());
			}
		}
		return labels;
	}

	private boolean currentlyEffective(FmEmployeeOrgAssignment assignment) {
		Date now = new Date();
		return !assignment.getEffectiveFrom().after(now)
				&& (assignment.getEffectiveTo() == null || assignment.getEffectiveTo().after(now));
	}

	private void validatePeriod(Date effectiveFrom, Date effectiveTo, String message)
			throws ServiceException {
		if (effectiveFrom == null || effectiveTo != null && !effectiveTo.after(effectiveFrom)) {
			throw new ServiceException(message);
		}
	}

	private void applyDuty(FmOrgDuty duty, FmOrgDutyCommand command) {
		duty.setOrgUnitId(command.orgUnitId());
		duty.setDutyName(command.dutyName());
		duty.setDutyType(command.dutyType());
		duty.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		duty.setEffectiveFrom(command.effectiveFrom());
		duty.setEffectiveTo(command.effectiveTo());
		duty.setDescription(command.description());
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
