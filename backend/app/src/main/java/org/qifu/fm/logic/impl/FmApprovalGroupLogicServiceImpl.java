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
import org.qifu.fm.dto.command.FmApprovalGroupCommand;
import org.qifu.fm.dto.command.FmApprovalGroupMemberCommand;
import org.qifu.fm.dto.view.FmApprovalGroupMemberView;
import org.qifu.fm.dto.view.FmApprovalGroupView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmApprovalGroup;
import org.qifu.fm.entity.FmApprovalGroupMember;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.logic.IFmApprovalGroupLogicService;
import org.qifu.fm.service.IFmApprovalGroupMemberService;
import org.qifu.fm.service.IFmApprovalGroupService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmApprovalGroupLogicServiceImpl implements IFmApprovalGroupLogicService {

	private static final List<String> ASSIGNMENT_MODES =
			List.of("CANDIDATE", "ALL", "SEQUENTIAL");

	private final IFmApprovalGroupService groupService;
	private final IFmApprovalGroupMemberService memberService;
	private final IFmEmployeeService employeeService;
	private final IFmTenantService tenantService;

	public FmApprovalGroupLogicServiceImpl(
			IFmApprovalGroupService groupService,
			IFmApprovalGroupMemberService memberService,
			IFmEmployeeService employeeService,
			IFmTenantService tenantService) {
		this.groupService = groupService;
		this.memberService = memberService;
		this.employeeService = employeeService;
		this.tenantService = tenantService;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApprovalGroupView> create(FmApprovalGroupCommand command)
			throws ServiceException {
		validateGroup(command);
		FmApprovalGroup group = new FmApprovalGroup();
		group.setTenantId(command.tenantId());
		group.setApprovalGroupId(UUID.randomUUID().toString());
		group.setGroupCode(command.groupCode());
		applyGroup(group, command);
		groupService.insert(group);
		return load(group.getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmApprovalGroupView> load(String oid, String message)
			throws ServiceException {
		FmApprovalGroup group = groupService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		DefaultResult<FmApprovalGroupView> result = success(view(group));
		result.setMessage(message);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApprovalGroupView> update(FmApprovalGroupCommand command)
			throws ServiceException {
		FmApprovalGroup group = groupService.selectByPrimaryKey(command.oid())
				.getValueEmptyThrowMessage();
		if (!group.getTenantId().equals(command.tenantId())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		validateGroup(command);
		applyGroup(group, command);
		groupService.update(group);
		return load(group.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApprovalGroupView> deactivate(String oid) throws ServiceException {
		FmApprovalGroup group = groupService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		group.setStatus("INACTIVE");
		groupService.update(group);
		return load(oid, BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApprovalGroupView> saveMember(FmApprovalGroupMemberCommand command)
			throws ServiceException {
		FmApprovalGroup group = groupService.selectByPrimaryKey(command.groupOid())
				.getValueEmptyThrowMessage();
		FmEmployee employee = requiredEmployee(group.getTenantId(), command.employeeId());
		validatePeriod(command.effectiveFrom(), command.effectiveTo());
		validateDuplicateMember(group, command);

		FmApprovalGroupMember member;
		if (StringUtils.isBlank(command.oid())) {
			member = new FmApprovalGroupMember();
			member.setTenantId(group.getTenantId());
			member.setApprovalGroupId(group.getApprovalGroupId());
			member.setApprovalGroupMemberId(UUID.randomUUID().toString());
		} else {
			member = memberService.selectByPrimaryKey(command.oid()).getValueEmptyThrowMessage();
			if (!group.getTenantId().equals(member.getTenantId())
					|| !group.getApprovalGroupId().equals(member.getApprovalGroupId())) {
				throw new ServiceException(BaseSystemMessage.parameterIncorrect());
			}
		}
		member.setEmployeeId(employee.getEmployeeId());
		member.setPriority(command.priority() == null ? 100 : command.priority());
		member.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		member.setEffectiveFrom(command.effectiveFrom());
		member.setEffectiveTo(command.effectiveTo());
		if (StringUtils.isBlank(command.oid())) {
			memberService.insert(member);
		} else {
			memberService.update(member);
		}
		return load(group.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmApprovalGroupView> deactivateMember(String groupOid, String oid)
			throws ServiceException {
		FmApprovalGroup group = groupService.selectByPrimaryKey(groupOid).getValueEmptyThrowMessage();
		FmApprovalGroupMember member = memberService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		if (!group.getTenantId().equals(member.getTenantId())
				|| !group.getApprovalGroupId().equals(member.getApprovalGroupId())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		member.setStatus("INACTIVE");
		if (member.getEffectiveTo() == null || member.getEffectiveTo().after(new Date())) {
			member.setEffectiveTo(new Date());
		}
		memberService.update(member);
		return load(groupOid, BaseSystemMessage.updateSuccess());
	}

	@Override
	public FmApprovalGroupView view(FmApprovalGroup group) throws ServiceException {
		Map<String, FmEmployee> employees = employeeMap(group.getTenantId());
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", group.getTenantId());
		params.put("approvalGroupId", group.getApprovalGroupId());
		List<FmApprovalGroupMemberView> members = memberService
				.selectListByParams(params, "PRIORITY,EFFECTIVE_FROM", "ASC").getValue().stream()
				.map(value -> {
					FmEmployee employee = employees.get(value.getEmployeeId());
					String label = employee == null ? value.getEmployeeId()
							: employee.getEmployeeNo() + "／" + employee.getDisplayName();
					return new FmApprovalGroupMemberView(
							value.getOid(), value.getApprovalGroupMemberId(), value.getEmployeeId(),
							label, value.getPriority(), value.getStatus(), value.getEffectiveFrom(),
							value.getEffectiveTo());
				})
				.toList();
		return new FmApprovalGroupView(
				group.getOid(), group.getTenantId(), group.getApprovalGroupId(),
				group.getGroupCode(), group.getGroupName(), group.getAssignmentMode(),
				group.getStatus(), group.getDescription(), members);
	}

	@Override
	public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("status", "ACTIVE");
		return success(tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue()
				.stream()
				.map(value -> new FmOptionView(value.getTenantId(),
						value.getTenantCode() + "／" + value.getTenantName()))
				.toList());
	}

	@Override
	public DefaultResult<List<FmOptionView>> employeeOptions(String groupOid)
			throws ServiceException {
		FmApprovalGroup group = groupService.selectByPrimaryKey(groupOid).getValueEmptyThrowMessage();
		return success(employeeMap(group.getTenantId()).values().stream()
				.map(value -> new FmOptionView(value.getEmployeeId(),
						value.getEmployeeNo() + "／" + value.getDisplayName()))
				.sorted((left, right) -> left.label().compareTo(right.label()))
				.toList());
	}

	private void validateGroup(FmApprovalGroupCommand command) throws ServiceException {
		if (StringUtils.isAnyBlank(command.tenantId(), command.groupCode(), command.groupName(),
				command.assignmentMode()) || !ASSIGNMENT_MODES.contains(command.assignmentMode())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private FmEmployee requiredEmployee(String tenantId, String employeeId)
			throws ServiceException {
		FmEmployee employee = employeeMap(tenantId).get(employeeId);
		if (employee == null) {
			throw new ServiceException("員工不存在、已停用或不屬於此 Tenant");
		}
		return employee;
	}

	private void validateDuplicateMember(
			FmApprovalGroup group,
			FmApprovalGroupMemberCommand command) throws ServiceException {
		if (!"ACTIVE".equals(StringUtils.defaultIfBlank(command.status(), "ACTIVE"))) {
			return;
		}
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", group.getTenantId());
		params.put("approvalGroupId", group.getApprovalGroupId());
		params.put("employeeId", command.employeeId());
		params.put("status", "ACTIVE");
		boolean duplicate = memberService.selectListByParams(params, "EFFECTIVE_FROM", "ASC")
				.getValue().stream()
				.anyMatch(value -> !value.getOid().equals(command.oid()));
		if (duplicate) {
			throw new ServiceException("此員工已是群組的有效成員");
		}
	}

	private void validatePeriod(Date effectiveFrom, Date effectiveTo) throws ServiceException {
		if (effectiveFrom == null || effectiveTo != null && !effectiveTo.after(effectiveFrom)) {
			throw new ServiceException("成員有效期間不正確");
		}
	}

	private Map<String, FmEmployee> employeeMap(String tenantId) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		return employeeService.selectListByParams(params, "EMPLOYEE_NO", "ASC").getValue().stream()
				.collect(Collectors.toMap(FmEmployee::getEmployeeId, Function.identity()));
	}

	private void applyGroup(FmApprovalGroup group, FmApprovalGroupCommand command) {
		group.setGroupName(command.groupName());
		group.setAssignmentMode(command.assignmentMode());
		group.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		group.setDescription(command.description());
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
