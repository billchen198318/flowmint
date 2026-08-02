package org.qifu.fm.logic.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.dto.command.FmWorkflowDelegationCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmWorkflowDelegationView;
import org.qifu.fm.entity.FmApprovalGroup;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.entity.FmWorkflowDelegation;
import org.qifu.fm.logic.IFmWorkflowDelegationLogicService;
import org.qifu.fm.service.IFmApprovalGroupService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmTenantService;
import org.qifu.fm.service.IFmWorkflowDelegationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmWorkflowDelegationLogicServiceImpl
		implements IFmWorkflowDelegationLogicService {

	private static final List<String> SCOPE_TYPES =
			List.of("ALL", "PROCESS", "APPROVAL_GROUP");

	private final IFmWorkflowDelegationService delegationService;
	private final IFmTenantService tenantService;
	private final IFmTenantAccountService tenantAccountService;
	private final IFmEmployeeService employeeService;
	private final IFmApprovalGroupService approvalGroupService;

	public FmWorkflowDelegationLogicServiceImpl(
			IFmWorkflowDelegationService delegationService,
			IFmTenantService tenantService,
			IFmTenantAccountService tenantAccountService,
			IFmEmployeeService employeeService,
			IFmApprovalGroupService approvalGroupService) {
		this.delegationService = delegationService;
		this.tenantService = tenantService;
		this.tenantAccountService = tenantAccountService;
		this.employeeService = employeeService;
		this.approvalGroupService = approvalGroupService;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmWorkflowDelegationView> create(FmWorkflowDelegationCommand command)
			throws ServiceException {
		validate(command);
		validateOverlap(command, null);
		FmWorkflowDelegation delegation = new FmWorkflowDelegation();
		delegation.setTenantId(command.tenantId());
		delegation.setDelegationId(UUID.randomUUID().toString());
		delegation.setPrincipalAccount(command.principalAccount());
		apply(delegation, command);
		delegationService.insert(delegation);
		return load(delegation.getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmWorkflowDelegationView> load(String oid, String message)
			throws ServiceException {
		FmWorkflowDelegation delegation = delegationService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		DefaultResult<FmWorkflowDelegationView> result = success(view(delegation));
		result.setMessage(message);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmWorkflowDelegationView> update(FmWorkflowDelegationCommand command)
			throws ServiceException {
		FmWorkflowDelegation delegation = delegationService.selectByPrimaryKey(command.oid())
				.getValueEmptyThrowMessage();
		FmWorkflowDelegationCommand normalized = new FmWorkflowDelegationCommand(
				command.oid(), delegation.getTenantId(), delegation.getPrincipalAccount(),
				command.delegateAccount(), command.scopeType(), command.scopeRefId(),
				command.allowRedelegate(), command.status(), command.effectiveFrom(),
				command.effectiveTo(), command.reason());
		validate(normalized);
		validateOverlap(normalized, delegation.getOid());
		apply(delegation, normalized);
		delegationService.update(delegation);
		return load(delegation.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmWorkflowDelegationView> deactivate(String oid) throws ServiceException {
		FmWorkflowDelegation delegation = delegationService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		delegation.setStatus("INACTIVE");
		delegationService.update(delegation);
		return load(oid, BaseSystemMessage.updateSuccess());
	}

	@Override
	public FmWorkflowDelegationView view(FmWorkflowDelegation delegation)
			throws ServiceException {
		Map<String, String> labels = accountLabels(delegation.getTenantId());
		String scopeLabel = delegation.getScopeRefId();
		if ("ALL".equals(delegation.getScopeType())) {
			scopeLabel = "全部流程";
		} else if ("APPROVAL_GROUP".equals(delegation.getScopeType())) {
			scopeLabel = groupLabel(delegation.getTenantId(), delegation.getScopeRefId());
		}
		return new FmWorkflowDelegationView(
				delegation.getOid(), delegation.getTenantId(), delegation.getDelegationId(),
				delegation.getPrincipalAccount(), labels.get(delegation.getPrincipalAccount()),
				delegation.getDelegateAccount(), labels.get(delegation.getDelegateAccount()),
				delegation.getScopeType(), delegation.getScopeRefId(), scopeLabel,
				delegation.getAllowRedelegate(), delegation.getStatus(),
				delegation.getEffectiveFrom(), delegation.getEffectiveTo(), delegation.getReason());
	}

	@Override
	public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("status", "ACTIVE");
		return success(tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue()
				.stream().map(value -> new FmOptionView(value.getTenantId(),
						value.getTenantCode() + "／" + value.getTenantName())).toList());
	}

	@Override
	public DefaultResult<List<FmOptionView>> accountOptions(String tenantId)
			throws ServiceException {
		return success(accountLabels(tenantId).entrySet().stream()
				.map(entry -> new FmOptionView(entry.getKey(), entry.getValue()))
				.sorted((left, right) -> left.label().compareTo(right.label())).toList());
	}

	@Override
	public DefaultResult<List<FmOptionView>> groupOptions(String tenantId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		return success(approvalGroupService.selectListByParams(params, "GROUP_CODE", "ASC")
				.getValue().stream().map(value -> new FmOptionView(value.getApprovalGroupId(),
						value.getGroupCode() + "／" + value.getGroupName())).toList());
	}

	private void validate(FmWorkflowDelegationCommand command) throws ServiceException {
		if (StringUtils.isAnyBlank(command.tenantId(), command.principalAccount(),
				command.delegateAccount(), command.scopeType(), command.reason())
				|| !SCOPE_TYPES.contains(command.scopeType())
				|| !List.of("Y", "N").contains(
						StringUtils.defaultIfBlank(command.allowRedelegate(), "N"))
				|| !List.of("ACTIVE", "INACTIVE").contains(
						StringUtils.defaultIfBlank(command.status(), "ACTIVE"))
				|| command.principalAccount().equals(command.delegateAccount())
				|| command.effectiveFrom() == null || command.effectiveTo() == null
				|| !command.effectiveTo().after(command.effectiveFrom())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		Map<String, String> accounts = accountLabels(command.tenantId());
		if (!accounts.containsKey(command.principalAccount())
				|| !accounts.containsKey(command.delegateAccount())) {
			throw new ServiceException("被代理人或代理人未啟用，或不屬於所選 Tenant");
		}
		if (!"ALL".equals(command.scopeType()) && StringUtils.isBlank(command.scopeRefId())) {
			throw new ServiceException("指定流程或簽核群組時必須選擇代理範圍");
		}
		if ("APPROVAL_GROUP".equals(command.scopeType())) {
			groupLabel(command.tenantId(), command.scopeRefId());
		}
	}

	private void validateOverlap(FmWorkflowDelegationCommand command, String excludedOid)
			throws ServiceException {
		if (!"ACTIVE".equals(StringUtils.defaultIfBlank(command.status(), "ACTIVE"))) {
			return;
		}
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", command.tenantId());
		params.put("principalAccount", command.principalAccount());
		params.put("scopeType", command.scopeType());
		params.put("status", "ACTIVE");
		String scopeRefId = "ALL".equals(command.scopeType()) ? null : command.scopeRefId();
		boolean overlaps = delegationService.selectListByParams(params, "EFFECTIVE_FROM", "ASC")
				.getValue().stream()
				.filter(value -> !Objects.equals(value.getOid(), excludedOid))
				.filter(value -> Objects.equals(value.getScopeRefId(), scopeRefId))
				.anyMatch(value -> command.effectiveFrom().before(value.getEffectiveTo())
						&& value.getEffectiveFrom().before(command.effectiveTo()));
		if (overlaps) {
			throw new ServiceException("同一被代理人、相同代理範圍的有效期間不可重疊");
		}
	}
	private Map<String, String> accountLabels(String tenantId) throws ServiceException {
		Map<String, Object> linkParams = new HashMap<>();
		linkParams.put("tenantId", tenantId);
		linkParams.put("status", "ACTIVE");
		List<FmTenantAccount> links = tenantAccountService
				.selectListByParams(linkParams, "ACCOUNT", "ASC").getValue();
		Map<String, Object> employeeParams = new HashMap<>();
		employeeParams.put("tenantId", tenantId);
		employeeParams.put("status", "ACTIVE");
		Map<String, FmEmployee> employees = employeeService
				.selectListByParams(employeeParams, "EMPLOYEE_NO", "ASC").getValue().stream()
				.collect(Collectors.toMap(FmEmployee::getAccount, Function.identity()));
		return links.stream().collect(Collectors.toMap(FmTenantAccount::getAccount, link -> {
			FmEmployee employee = employees.get(link.getAccount());
			return employee == null ? link.getAccount()
					: employee.getEmployeeNo() + "／" + employee.getDisplayName()
						+ "（" + link.getAccount() + "）";
		}, (left, right) -> left));
	}

	private String groupLabel(String tenantId, String groupId) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("approvalGroupId", groupId);
		params.put("status", "ACTIVE");
		FmApprovalGroup group = approvalGroupService.selectListByParams(params, "GROUP_CODE", "ASC")
				.getValue().stream().findFirst()
				.orElseThrow(() -> new ServiceException("簽核群組不存在、已停用或不屬於此 Tenant"));
		return group.getGroupCode() + "／" + group.getGroupName();
	}

	private void apply(FmWorkflowDelegation delegation,
			FmWorkflowDelegationCommand command) {
		delegation.setDelegateAccount(command.delegateAccount());
		delegation.setScopeType(command.scopeType());
		delegation.setScopeRefId("ALL".equals(command.scopeType()) ? null : command.scopeRefId());
		delegation.setAllowRedelegate(StringUtils.defaultIfBlank(command.allowRedelegate(), "N"));
		delegation.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		delegation.setEffectiveFrom(command.effectiveFrom());
		delegation.setEffectiveTo(command.effectiveTo());
		delegation.setReason(command.reason());
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
