package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.dto.command.FmOrgUnitHeadCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgUnitHeadView;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.logic.IFmOrgUnitHeadLogicService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmOrgUnitHeadLogicServiceImpl implements IFmOrgUnitHeadLogicService {

	private static final List<String> HEAD_TYPES = List.of("HEAD", "DEPUTY_HEAD", "ACTING_HEAD");

	private final IFmOrgUnitHeadService headService;
	private final IFmOrgUnitVersionService orgUnitVersionService;
	private final IFmEmployeeOrgAssignmentService assignmentService;
	private final IFmEmployeeService employeeService;
	private final IFmTenantService tenantService;
	private final FmTenantAccessGuard tenantAccessGuard;

	public FmOrgUnitHeadLogicServiceImpl(IFmOrgUnitHeadService headService,
			IFmOrgUnitVersionService orgUnitVersionService,
			IFmEmployeeOrgAssignmentService assignmentService,
			IFmEmployeeService employeeService,
			IFmTenantService tenantService, FmTenantAccessGuard tenantAccessGuard) {
		this.headService = headService;
		this.orgUnitVersionService = orgUnitVersionService;
		this.assignmentService = assignmentService;
		this.employeeService = employeeService;
		this.tenantService = tenantService;
		this.tenantAccessGuard = tenantAccessGuard;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgUnitHeadView> create(FmOrgUnitHeadCommand command) throws ServiceException {
		tenantAccessGuard.requireAccess(command.tenantId());
		validate(command, null);
		FmOrgUnitHead value = new FmOrgUnitHead();
		value.setTenantId(command.tenantId());
		value.setOrgUnitHeadId(UUID.randomUUID().toString());
		apply(value, command);
		headService.insert(value);
		return load(value.getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmOrgUnitHeadView> load(String oid, String message) throws ServiceException {
		FmOrgUnitHead value = headService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(value.getTenantId());
		DefaultResult<FmOrgUnitHeadView> result = success(view(value));
		result.setMessage(message);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgUnitHeadView> update(FmOrgUnitHeadCommand command) throws ServiceException {
		FmOrgUnitHead value = headService.selectByPrimaryKey(command.oid()).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(value.getTenantId());
		if (!value.getTenantId().equals(command.tenantId())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		validate(command, value.getOid());
		apply(value, command);
		headService.update(value);
		return load(value.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgUnitHeadView> deactivate(String oid) throws ServiceException {
		FmOrgUnitHead value = headService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		tenantAccessGuard.requireAccess(value.getTenantId());
		value.setStatus("INACTIVE");
		Date now = new Date();
		if (!value.getEffectiveFrom().after(now)
				&& (value.getEffectiveTo() == null || value.getEffectiveTo().after(now))) {
			value.setEffectiveTo(now);
		}
		headService.update(value);
		return load(oid, BaseSystemMessage.updateSuccess());
	}

	@Override
	public FmOrgUnitHeadView view(FmOrgUnitHead value) throws ServiceException {
		tenantAccessGuard.requireAccess(value.getTenantId());
		String unitLabel = orgUnitOptions(value.getTenantId()).getValue().stream()
				.filter(option -> option.value().equals(value.getOrgUnitId()))
				.map(FmOptionView::label).findFirst().orElse(value.getOrgUnitId());
		String employeeLabel = employeeOptions(value.getTenantId(), value.getOrgUnitId()).getValue().stream()
				.filter(option -> option.value().equals(value.getEmployeeId()))
				.map(FmOptionView::label).findFirst().orElse(value.getEmployeeId());
		return new FmOrgUnitHeadView(value.getOid(), value.getTenantId(), value.getOrgUnitHeadId(),
				value.getOrgUnitId(), unitLabel, value.getEmployeeId(), employeeLabel, value.getHeadType(),
				value.getPriority(), value.getStatus(), value.getEffectiveFrom(), value.getEffectiveTo(),
				value.getDescription());
	}

	@Override
	public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("status", "ACTIVE");
		var readableTenantIds = tenantAccessGuard.readableTenantIds();
		return success(tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue().stream()
				.filter(value -> readableTenantIds.contains(value.getTenantId()))
				.map(value -> new FmOptionView(value.getTenantId(),
						value.getTenantCode() + "／" + value.getTenantName())).toList());
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
						value.getUnitCode() + "／" + value.getUnitName())).toList());
	}

	@Override
	public DefaultResult<List<FmOptionView>> employeeOptions(String tenantId, String orgUnitId)
			throws ServiceException {
		if (StringUtils.isAnyBlank(tenantId, orgUnitId)) {
			return success(List.of());
		}
		tenantAccessGuard.requireAccess(tenantId);
		Date now = new Date();
		return eligibleEmployees(tenantId, orgUnitId, now, now);
	}

	private DefaultResult<List<FmOptionView>> eligibleEmployees(String tenantId, String orgUnitId,
			Date from, Date to) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("orgUnitId", orgUnitId);
		params.put("status", "ACTIVE");
		List<FmEmployeeOrgAssignment> assignments = assignmentService
				.selectListByParams(params, "IS_PRIMARY DESC,EFFECTIVE_FROM", "ASC").getValue();
		Map<String, FmEmployee> employees = employeeMap(tenantId, from, to);
		return success(assignments.stream()
				.filter(assignment -> "ACTIVE".equals(assignment.getStatus())
						&& covers(assignment.getEffectiveFrom(), assignment.getEffectiveTo(), from, to))
				.map(FmEmployeeOrgAssignment::getEmployeeId).distinct()
				.filter(employees::containsKey).map(employeeId -> {
					FmEmployee employee = employees.get(employeeId);
					return new FmOptionView(employeeId,
							employee.getEmployeeNo() + "／" + employee.getDisplayName());
				}).toList());
	}

	private void validate(FmOrgUnitHeadCommand command, String currentOid) throws ServiceException {
		if (StringUtils.isAnyBlank(command.tenantId(), command.orgUnitId(), command.employeeId(),
				command.headType()) || !HEAD_TYPES.contains(command.headType())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		if (command.effectiveFrom() == null || command.effectiveTo() != null
				&& !command.effectiveTo().after(command.effectiveFrom())) {
			throw new ServiceException("主管配置有效期間不正確");
		}
		if (orgUnitOptions(command.tenantId()).getValue().stream()
				.noneMatch(value -> value.value().equals(command.orgUnitId()))) {
			throw new ServiceException("部門不存在、已停用或不屬於所選 Tenant");
		}
		if (eligibleEmployees(command.tenantId(), command.orgUnitId(),
				command.effectiveFrom(), command.effectiveTo()).getValue().stream()
				.noneMatch(value -> value.value().equals(command.employeeId()))) {
			throw new ServiceException("所選員工沒有此部門的有效任職");
		}
		if ("HEAD".equals(command.headType()) && "ACTIVE".equals(defaultStatus(command.status()))) {
			Map<String, Object> params = new HashMap<>();
			params.put("tenantId", command.tenantId());
			params.put("orgUnitId", command.orgUnitId());
			params.put("headType", "HEAD");
			params.put("status", "ACTIVE");
			boolean overlap = headService.selectListByParams(params, "EFFECTIVE_FROM", "ASC").getValue().stream()
					.filter(value -> !value.getOid().equals(currentOid))
					.anyMatch(value -> overlaps(command.effectiveFrom(), command.effectiveTo(),
							value.getEffectiveFrom(), value.getEffectiveTo()));
			if (overlap) {
				throw new ServiceException("同一部門在重疊期間只能有一位主要主管");
			}
		}
	}

	private Map<String, FmEmployee> employeeMap(String tenantId, Date from, Date to) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		Map<String, FmEmployee> values = new HashMap<>();
		for (FmEmployee employee : employeeService.selectListByParams(params, "EMPLOYEE_NO", "ASC").getValue()) {
			if ("ACTIVE".equals(employee.getStatus())
					&& covers(employee.getEffectiveFrom(), employee.getEffectiveTo(), from, to))
				values.put(employee.getEmployeeId(), employee);
		}
		return values;
	}

	private boolean covers(Date effectiveFrom, Date effectiveTo, Date from, Date to) {
		return effectiveFrom != null && !effectiveFrom.after(from)
				&& (effectiveTo == null || effectiveTo.after(from))
				&& (to != null ? effectiveTo == null || !effectiveTo.before(to) : effectiveTo == null);
	}

	private boolean overlaps(Date start1, Date end1, Date start2, Date end2) {
		return (end1 == null || start2.before(end1)) && (end2 == null || start1.before(end2));
	}

	private void apply(FmOrgUnitHead value, FmOrgUnitHeadCommand command) {
		value.setOrgUnitId(command.orgUnitId());
		value.setEmployeeId(command.employeeId());
		value.setHeadType(command.headType());
		value.setPriority(command.priority() == null ? 100 : command.priority());
		value.setStatus(defaultStatus(command.status()));
		value.setEffectiveFrom(command.effectiveFrom());
		value.setEffectiveTo(command.effectiveTo());
		value.setDescription(command.description());
	}

	private String defaultStatus(String status) {
		return StringUtils.defaultIfBlank(status, "ACTIVE");
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
