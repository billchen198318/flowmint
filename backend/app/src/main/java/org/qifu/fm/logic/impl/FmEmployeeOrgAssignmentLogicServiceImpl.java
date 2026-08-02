package org.qifu.fm.logic.impl;

import java.util.ArrayList;
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
import org.qifu.fm.dto.command.FmEmployeeOrgAssignmentCommand;
import org.qifu.fm.dto.view.FmEmployeeOrgAssignmentView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.logic.IFmEmployeeOrgAssignmentLogicService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmEmployeeOrgAssignmentLogicServiceImpl implements IFmEmployeeOrgAssignmentLogicService {

	private static final List<String> MANAGER_SOURCES = List.of("ORG_HEAD", "PARENT_HEAD", "EXPLICIT", "NONE");

	private final IFmEmployeeService employeeService;
	private final IFmEmployeeOrgAssignmentService assignmentService;
	private final IFmOrgUnitVersionService orgUnitVersionService;
	private final IFmOrgTitleService titleService;

	public FmEmployeeOrgAssignmentLogicServiceImpl(IFmEmployeeService employeeService,
			IFmEmployeeOrgAssignmentService assignmentService,
			IFmOrgUnitVersionService orgUnitVersionService,
			IFmOrgTitleService titleService) {
		this.employeeService = employeeService;
		this.assignmentService = assignmentService;
		this.orgUnitVersionService = orgUnitVersionService;
		this.titleService = titleService;
	}

	@Override
	public DefaultResult<List<FmEmployeeOrgAssignmentView>> list(String employeeOid) throws ServiceException {
		FmEmployee employee = requiredEmployee(employeeOid);
		List<FmEmployeeOrgAssignment> assignments = assignments(employee);
		Map<String, String> unitLabels = unitLabels(employee.getTenantId());
		Map<String, String> titleLabels = titleLabels(employee.getTenantId());
		Map<String, String> managerLabels = managerLabels(employee.getTenantId());
		List<FmEmployeeOrgAssignmentView> values = assignments.stream()
				.map(value -> new FmEmployeeOrgAssignmentView(
						value.getOid(), value.getEmployeeOrgAssignmentId(), value.getOrgUnitId(),
						unitLabels.getOrDefault(value.getOrgUnitId(), value.getOrgUnitId()), value.getTitleId(),
						titleLabels.getOrDefault(value.getTitleId(), value.getTitleId()), value.getManagerSource(),
						value.getDirectManagerAssignmentId(),
						managerLabels.getOrDefault(value.getDirectManagerAssignmentId(), ""), value.getIsPrimary(),
						value.getStatus(), value.getEffectiveFrom(), value.getEffectiveTo()))
				.toList();
		return success(values);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<List<FmEmployeeOrgAssignmentView>> save(FmEmployeeOrgAssignmentCommand command)
			throws ServiceException {
		FmEmployee employee = requiredEmployee(command.employeeOid());
		validate(command, employee);
		FmEmployeeOrgAssignment assignment;
		if (StringUtils.isBlank(command.oid())) {
			assignment = new FmEmployeeOrgAssignment();
			assignment.setTenantId(employee.getTenantId());
			assignment.setEmployeeId(employee.getEmployeeId());
			assignment.setEmployeeOrgAssignmentId(UUID.randomUUID().toString());
		} else {
			assignment = assignmentService.selectByPrimaryKey(command.oid()).getValueEmptyThrowMessage();
			if (!employee.getTenantId().equals(assignment.getTenantId())
					|| !employee.getEmployeeId().equals(assignment.getEmployeeId())) {
				throw new ServiceException(BaseSystemMessage.parameterIncorrect());
			}
		}
		apply(assignment, command);
		if (StringUtils.isBlank(command.oid())) {
			assignmentService.insert(assignment);
		} else {
			assignmentService.update(assignment);
		}
		return list(employee.getOid());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<List<FmEmployeeOrgAssignmentView>> deactivate(String employeeOid, String oid)
			throws ServiceException {
		FmEmployee employee = requiredEmployee(employeeOid);
		FmEmployeeOrgAssignment assignment = assignmentService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		if (!employee.getTenantId().equals(assignment.getTenantId())
				|| !employee.getEmployeeId().equals(assignment.getEmployeeId())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		assignment.setStatus("INACTIVE");
		if (assignment.getEffectiveTo() == null || assignment.getEffectiveTo().after(new Date())) {
			assignment.setEffectiveTo(new Date());
		}
		assignmentService.update(assignment);
		return list(employeeOid);
	}

	@Override
	public DefaultResult<List<FmOptionView>> orgUnitOptions(String employeeOid) throws ServiceException {
		FmEmployee employee = requiredEmployee(employeeOid);
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", employee.getTenantId());
		params.put("status", "ACTIVE");
		List<FmOptionView> values = orgUnitVersionService.selectCurrentTree(params).getValue().stream()
				.map(value -> new FmOptionView(value.getOrgUnitId(),
						value.getUnitCode() + "／" + value.getUnitName()))
				.toList();
		return success(values);
	}

	@Override
	public DefaultResult<List<FmOptionView>> titleOptions(String employeeOid) throws ServiceException {
		FmEmployee employee = requiredEmployee(employeeOid);
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", employee.getTenantId());
		params.put("status", "ACTIVE");
		List<FmOptionView> values = titleService.selectListByParams(params, "SORT_NO,TITLE_CODE", "ASC").getValue()
				.stream().map(value -> new FmOptionView(value.getTitleId(),
						value.getTitleCode() + "／" + value.getTitleName()))
				.toList();
		return success(values);
	}

	@Override
	public DefaultResult<List<FmOptionView>> managerOptions(String employeeOid) throws ServiceException {
		FmEmployee employee = requiredEmployee(employeeOid);
		Map<String, String> labels = managerLabels(employee.getTenantId());
		List<FmOptionView> values = labels.entrySet().stream()
				.filter(entry -> !entry.getValue().startsWith(employee.getEmployeeNo() + "／"))
				.map(entry -> new FmOptionView(entry.getKey(), entry.getValue()))
				.sorted((left, right) -> left.label().compareTo(right.label()))
				.toList();
		return success(values);
	}

	private void validate(FmEmployeeOrgAssignmentCommand command, FmEmployee employee) throws ServiceException {
		if (StringUtils.isAnyBlank(command.orgUnitId(), command.titleId(), command.managerSource())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		if (!MANAGER_SOURCES.contains(command.managerSource())) {
			throw new ServiceException("直屬主管來源不正確");
		}
		if (command.effectiveFrom() == null
				|| command.effectiveTo() != null && !command.effectiveTo().after(command.effectiveFrom())) {
			throw new ServiceException("任職有效期間不正確");
		}
		if ("EXPLICIT".equals(command.managerSource()) != StringUtils.isNotBlank(command.directManagerAssignmentId())) {
			throw new ServiceException("指定直屬主管時必須選擇有效主管任職");
		}
		if (!unitLabels(employee.getTenantId()).containsKey(command.orgUnitId())) {
			throw new ServiceException("部門不存在、已停用或不屬於此 Tenant");
		}
		if (!titleLabels(employee.getTenantId()).containsKey(command.titleId())) {
			throw new ServiceException("職稱不存在、已停用或不屬於此 Tenant");
		}
		if ("Y".equals(StringUtils.defaultIfBlank(command.isPrimary(), "N"))) {
			boolean duplicate = assignments(employee).stream()
					.anyMatch(value -> "Y".equals(value.getIsPrimary()) && "ACTIVE".equals(value.getStatus())
							&& !value.getOid().equals(command.oid()));
			if (duplicate) {
				throw new ServiceException("同一員工只能有一筆有效的主要任職");
			}
		}
		if ("EXPLICIT".equals(command.managerSource())) {
			validateExplicitManager(command, employee);
		}
	}

	private void validateExplicitManager(FmEmployeeOrgAssignmentCommand command, FmEmployee employee)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", employee.getTenantId());
		params.put("status", "ACTIVE");
		FmEmployeeOrgAssignment manager = assignmentService.selectListByParams(params,
				"EMPLOYEE_ORG_ASSIGNMENT_ID", "ASC").getValue().stream()
				.filter(value -> value.getEmployeeOrgAssignmentId().equals(command.directManagerAssignmentId()))
				.findFirst().orElseThrow(() -> new ServiceException("指定的直屬主管任職不存在或已停用"));
		if (employee.getEmployeeId().equals(manager.getEmployeeId())) {
			throw new ServiceException("直屬主管不可指定自己");
		}
	}

	private List<FmEmployeeOrgAssignment> assignments(FmEmployee employee) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", employee.getTenantId());
		params.put("employeeId", employee.getEmployeeId());
		return assignmentService.selectListByParams(params, "IS_PRIMARY DESC,EFFECTIVE_FROM", "ASC").getValue();
	}

	private Map<String, String> unitLabels(String tenantId) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		return orgUnitVersionService.selectCurrentTree(params).getValue().stream()
				.collect(Collectors.toMap(FmOrgUnitView::getOrgUnitId,
						value -> value.getUnitCode() + "／" + value.getUnitName()));
	}

	private Map<String, String> titleLabels(String tenantId) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		return titleService.selectListByParams(params, "SORT_NO,TITLE_CODE", "ASC").getValue().stream()
				.collect(Collectors.toMap(FmOrgTitle::getTitleId,
						value -> value.getTitleCode() + "／" + value.getTitleName()));
	}

	private Map<String, String> managerLabels(String tenantId) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		Map<String, FmEmployee> employees = employeeService.selectListByParams(params, "EMPLOYEE_NO", "ASC").getValue()
				.stream().collect(Collectors.toMap(FmEmployee::getEmployeeId, Function.identity()));
		Map<String, String> units = unitLabels(tenantId);
		List<FmEmployeeOrgAssignment> assignments = assignmentService.selectListByParams(params,
				"EMPLOYEE_ORG_ASSIGNMENT_ID", "ASC").getValue();
		Map<String, String> values = new HashMap<>();
		for (FmEmployeeOrgAssignment assignment : assignments) {
			FmEmployee employee = employees.get(assignment.getEmployeeId());
			if (employee != null) {
				values.put(assignment.getEmployeeOrgAssignmentId(), employee.getEmployeeNo() + "／"
						+ employee.getDisplayName() + "／" + units.getOrDefault(assignment.getOrgUnitId(), ""));
			}
		}
		return values;
	}

	private FmEmployee requiredEmployee(String oid) throws ServiceException {
		if (StringUtils.isBlank(oid)) {
			throw new ServiceException(BaseSystemMessage.parameterBlank());
		}
		return employeeService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
	}

	private void apply(FmEmployeeOrgAssignment assignment, FmEmployeeOrgAssignmentCommand command) {
		assignment.setOrgUnitId(command.orgUnitId());
		assignment.setTitleId(command.titleId());
		assignment.setManagerSource(command.managerSource());
		assignment.setDirectManagerAssignmentId("EXPLICIT".equals(command.managerSource())
				? command.directManagerAssignmentId() : null);
		assignment.setIsPrimary(StringUtils.defaultIfBlank(command.isPrimary(), "N"));
		assignment.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		assignment.setEffectiveFrom(command.effectiveFrom());
		assignment.setEffectiveTo(command.effectiveTo());
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
