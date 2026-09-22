package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.view.FmOrganizationHealthIssueView;
import org.qifu.fm.dto.view.FmOrganizationHealthView;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.entity.FmWorkflowDelegation;
import org.qifu.fm.logic.IFmOrganizationHealthLogicService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmWorkflowDelegationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmOrganizationHealthLogicServiceImpl implements IFmOrganizationHealthLogicService {

	private final IFmEmployeeService employeeService;
	private final IFmEmployeeOrgAssignmentService assignmentService;
	private final IFmOrgUnitVersionService orgUnitVersionService;
	private final IFmOrgUnitHeadService headService;
	private final IFmWorkflowDelegationService delegationService;
	private final IFmTenantAccountService tenantAccountService;
	private final Clock clock;

	@Autowired
	public FmOrganizationHealthLogicServiceImpl(IFmEmployeeService employeeService,
			IFmEmployeeOrgAssignmentService assignmentService,
			IFmOrgUnitVersionService orgUnitVersionService,
			IFmOrgUnitHeadService headService,
			IFmWorkflowDelegationService delegationService,
			IFmTenantAccountService tenantAccountService) {
		this(employeeService, assignmentService, orgUnitVersionService, headService,
				delegationService, tenantAccountService, Clock.systemUTC());
	}

	FmOrganizationHealthLogicServiceImpl(IFmEmployeeService employeeService,
			IFmEmployeeOrgAssignmentService assignmentService,
			IFmOrgUnitVersionService orgUnitVersionService,
			IFmOrgUnitHeadService headService,
			IFmWorkflowDelegationService delegationService,
			IFmTenantAccountService tenantAccountService, Clock clock) {
		this.employeeService = employeeService;
		this.assignmentService = assignmentService;
		this.orgUnitVersionService = orgUnitVersionService;
		this.headService = headService;
		this.delegationService = delegationService;
		this.tenantAccountService = tenantAccountService;
		this.clock = clock;
	}

	@Override
	public DefaultResult<FmOrganizationHealthView> inspect(String tenantId) throws ServiceException {
		if (StringUtils.isBlank(tenantId)) {
			throw new ServiceException("Tenant 不可空白");
		}
		Date now = Date.from(Instant.now(clock));
		requireMembership(tenantId, now);
		Map<String, Object> active = parameters(tenantId);
		List<FmOrgUnitView> units = orgUnitVersionService.selectCurrentTree(active).getValue();
		List<FmEmployee> employees = employeeService
				.selectListByParams(active, "EMPLOYEE_NO", "ASC").getValue();
		List<FmEmployeeOrgAssignment> assignments = assignmentService
				.selectListByParams(active, "EMPLOYEE_ID,IS_PRIMARY", "ASC").getValue();
		List<FmOrgUnitHead> heads = headService
				.selectListByParams(active, "ORG_UNIT_ID,PRIORITY", "ASC").getValue();
		List<FmWorkflowDelegation> delegations = delegationService
				.selectListByParams(active, "PRINCIPAL_ACCOUNT,EFFECTIVE_FROM", "ASC").getValue();
		units = units.stream().filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(), now)).toList();
		employees = employees.stream().filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(), now)).toList();
		assignments = assignments.stream().filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(), now)).toList();
		heads = heads.stream().filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(), now)).toList();
		delegations = delegations.stream().filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(), now)).toList();

		List<FmOrganizationHealthIssueView> headIssues = inspectHeads(units, employees, assignments, heads);
		List<FmOrganizationHealthIssueView> managerIssues = inspectManagers(
				units, employees, assignments, heads);
		List<FmOrganizationHealthIssueView> organizationIssues = inspectOrganization(units);
		List<FmOrganizationHealthIssueView> delegationIssues = inspectDelegations(employees, delegations);
		return success(new FmOrganizationHealthView(tenantId, now, headIssues, managerIssues,
				organizationIssues, delegationIssues));
	}

	private List<FmOrganizationHealthIssueView> inspectHeads(List<FmOrgUnitView> units,
			List<FmEmployee> employees, List<FmEmployeeOrgAssignment> assignments,
			List<FmOrgUnitHead> heads) {
		Set<String> employeeIds = employees.stream().map(FmEmployee::getEmployeeId).collect(Collectors.toSet());
		Map<String, FmEmployee> employeesById = employees.stream().collect(Collectors.toMap(
				FmEmployee::getEmployeeId, Function.identity(), (left, right) -> left));
		Set<String> assignmentKeys = assignments.stream()
				.map(value -> value.getOrgUnitId() + "\u0000" + value.getEmployeeId())
				.collect(Collectors.toSet());
		List<FmOrganizationHealthIssueView> issues = new ArrayList<>();
		for (FmOrgUnitView unit : units.stream().filter(value -> !"Y".equals(value.getIsVirtual())).toList()) {
			List<FmOrgUnitHead> current = heads.stream().filter(value -> "HEAD".equals(value.getHeadType())
					&& unit.getOrgUnitId().equals(value.getOrgUnitId())).toList();
			if (current.isEmpty()) {
				issues.add(issue("HEAD", "ORG_HEAD_MISSING", "WARNING", "未配置部門主管",
						unit.getOrgUnitId(), label(unit), "目前沒有有效正式主管。",
						"依賴部門主管的流程可能找不到簽核人。", "有效正式主管數量：0",
						"配置一位具有效部門任職的正式主管。", "FM_PROG002D0005", List.of()));
			} else if (current.size() > 1) {
				issues.add(issue("HEAD", "ORG_HEAD_MULTIPLE", "ERROR", "同時存在多位部門主管",
						unit.getOrgUnitId(), label(unit), "目前同時存在 " + current.size() + " 位有效正式主管。",
						"簽核人解析結果不唯一。", "有效正式主管數量：" + current.size(),
						"核對生效期間，調整為同一時間只有一位正式主管。", "FM_PROG002D0005", List.of()));
			}
			for (FmOrgUnitHead head : current) {
				if (!employeeIds.contains(head.getEmployeeId()) || !assignmentKeys.contains(
						unit.getOrgUnitId() + "\u0000" + head.getEmployeeId())) {
					issues.add(issue("HEAD", "ORG_HEAD_INVALID", "ERROR", "部門主管資料無效",
							head.getOrgUnitHeadId(), label(unit), "主管員工或其部門任職已失效。",
							"流程可能無法把工作指派給該主管。", "部門：「" + label(unit) + "」；主管：「"
									+ employeeLabel(employeesById.get(head.getEmployeeId()), head.getEmployeeId()) + "」。",
							"重新配置具有效員工及部門任職的主管。", "FM_PROG002D0005", List.of()));
				}
			}
		}
		return issues;
	}

	private List<FmOrganizationHealthIssueView> inspectManagers(List<FmOrgUnitView> units,
			List<FmEmployee> employees, List<FmEmployeeOrgAssignment> assignments,
			List<FmOrgUnitHead> heads) {
		Map<String, FmEmployeeOrgAssignment> byId = assignments.stream().collect(Collectors.toMap(
				FmEmployeeOrgAssignment::getEmployeeOrgAssignmentId, Function.identity(), (left, right) -> left));
		Map<String, FmOrgUnitView> unitsById = units.stream().collect(Collectors.toMap(
				FmOrgUnitView::getOrgUnitId, Function.identity(), (left, right) -> left));
		Map<String, FmEmployee> employeesById = employees.stream().collect(Collectors.toMap(
				FmEmployee::getEmployeeId, Function.identity(), (left, right) -> left));
		Map<String, String> headByUnit = heads.stream().filter(value -> "HEAD".equals(value.getHeadType()))
				.collect(Collectors.toMap(FmOrgUnitHead::getOrgUnitId, FmOrgUnitHead::getEmployeeId,
						(left, right) -> left));
		List<FmOrganizationHealthIssueView> issues = new ArrayList<>();
		Map<String, String> assignmentLabels = assignments.stream().collect(Collectors.toMap(
				FmEmployeeOrgAssignment::getEmployeeOrgAssignmentId,
				value -> assignmentLabel(value, employeesById, unitsById), (left, right) -> left));
		assignments.stream().filter(value -> "Y".equals(value.getIsPrimary())).collect(
				Collectors.groupingBy(FmEmployeeOrgAssignment::getEmployeeId)).forEach((employeeId, values) -> {
					if (values.size() > 1) {
						issues.add(issue("MANAGER", "PRIMARY_ASSIGNMENT_MULTIPLE", "ERROR",
								"同時存在多筆主要任職", employeeId,
								employeeLabel(employeesById.get(employeeId), employeeId),
								"目前有 " + values.size() + " 筆有效主要任職。", "主管鏈起點不唯一。",
								"主要任職部門：" + values.stream().map(assignmentLabels::get)
										.collect(Collectors.joining("、")), "保留一筆正確的主要任職。",
								"FM_PROG002D0001", List.of()));
					}
				});
		Map<String, String> managerByAssignment = new HashMap<>();
		for (FmEmployeeOrgAssignment assignment : assignments) {
			String managerAssignmentId = managerAssignment(
					assignment, assignments, byId, unitsById, headByUnit);
			String employeeLabel = assignmentLabel(assignment, employeesById, unitsById);
			String unitLabel = unitsById.containsKey(assignment.getOrgUnitId())
					? label(unitsById.get(assignment.getOrgUnitId())) : "未知部門";
			if (managerAssignmentId == null && !"NONE".equals(assignment.getManagerSource())) {
				issues.add(issue("MANAGER", "DIRECT_MANAGER_MISSING", "WARNING", "未配置有效直屬主管",
						assignment.getEmployeeOrgAssignmentId(), employeeLabel,
						"此任職無法依「" + managerSourceLabel(assignment.getManagerSource())
								+ "」找到有效直屬主管。",
						"需要直屬主管簽核時，流程可能暫停並通知管理人員處理。",
						"任職部門：「" + unitLabel + "」；主管來源：「"
								+ managerSourceLabel(assignment.getManagerSource()) + "」。",
						managerRecommendation(assignment.getManagerSource()),
						"FM_PROG002D0001", List.of()));
			} else if (managerAssignmentId != null) {
				managerByAssignment.put(assignment.getEmployeeOrgAssignmentId(), managerAssignmentId);
				if (assignment.getEmployeeOrgAssignmentId().equals(managerAssignmentId)) {
					issues.add(issue("MANAGER", "DIRECT_MANAGER_SELF", "ERROR", "直屬主管為本人",
							assignment.getEmployeeOrgAssignmentId(), employeeLabel,
							"任職將自己解析為直屬主管。", "主管鏈無法向上推進。",
							"任職與直屬主管皆為：「" + employeeLabel + "」。", "改設其他有效主管。",
							"FM_PROG002D0001", List.of(employeeLabel)));
				}
			}
		}
		issues.addAll(cycleIssues(managerByAssignment, assignmentLabels, "MANAGER",
				"DIRECT_MANAGER_CYCLE", "主管鏈形成循環", "FM_PROG002D0001"));
		return issues;
	}

	private List<FmOrganizationHealthIssueView> inspectOrganization(List<FmOrgUnitView> units) {
		Map<String, FmOrgUnitView> byId = units.stream().collect(Collectors.toMap(
				FmOrgUnitView::getOrgUnitId, Function.identity(), (left, right) -> left));
		Map<String, String> unitLabels = units.stream().collect(Collectors.toMap(
				FmOrgUnitView::getOrgUnitId, this::label, (left, right) -> left));
		List<FmOrganizationHealthIssueView> issues = new ArrayList<>();
		List<FmOrgUnitView> roots = units.stream().filter(value -> StringUtils.isBlank(value.getParentOrgUnitId())).toList();
		if (roots.size() > 1) {
			issues.add(issue("ORGANIZATION", "ORG_ROOT_MULTIPLE", "ERROR", "存在多個最高部門",
					"ROOT", "組織樹", "目前有 " + roots.size() + " 個無上級部門的有效節點。",
					"組織路徑及上層主管解析不唯一。", roots.stream().map(this::label).collect(Collectors.joining("、")),
					"確認唯一最高部門，為其他節點配置有效上級部門。", "FM_PROG002D0002", List.of()));
		}
		Map<String, String> parents = new HashMap<>();
		for (FmOrgUnitView unit : units) {
			if (StringUtils.isNotBlank(unit.getParentOrgUnitId())) {
				parents.put(unit.getOrgUnitId(), unit.getParentOrgUnitId());
				if (unit.getOrgUnitId().equals(unit.getParentOrgUnitId())) {
					issues.add(issue("ORGANIZATION", "PARENT_ORG_SELF", "ERROR", "上級部門為自己",
							unit.getOrgUnitId(), label(unit), "部門將自己設定為上級部門。",
							"組織樹形成循環。", "問題部門：「" + label(unit) + "」。", "配置正確的上級部門。",
							"FM_PROG002D0002", List.of(label(unit))));
				} else if (!byId.containsKey(unit.getParentOrgUnitId())) {
					issues.add(issue("ORGANIZATION", "PARENT_ORG_INVALID", "ERROR", "上級部門無效",
							unit.getOrgUnitId(), label(unit), "上級部門不存在或已失效。",
							"組織路徑及主管鏈可能中斷。", "問題部門：「" + label(unit)
									+ "」；其上級部門不存在或已失效。",
							"配置同 Tenant 的有效上級部門。", "FM_PROG002D0002", List.of()));
				}
			}
		}
		issues.addAll(cycleIssues(parents, unitLabels, "ORGANIZATION", "ORG_TREE_CYCLE",
				"組織樹形成循環", "FM_PROG002D0002"));
		return issues;
	}

	private List<FmOrganizationHealthIssueView> inspectDelegations(List<FmEmployee> employees,
			List<FmWorkflowDelegation> values) {
		List<FmOrganizationHealthIssueView> issues = new ArrayList<>();
		Map<String, String> accountLabels = employees.stream()
				.filter(value -> StringUtils.isNotBlank(value.getAccount()))
				.collect(Collectors.toMap(FmEmployee::getAccount,
						value -> employeeLabel(value, value.getAccount()), (left, right) -> left));
		Map<String, String> edges = new HashMap<>();
		for (FmWorkflowDelegation value : values) {
			if (value.getPrincipalAccount().equals(value.getDelegateAccount())) {
				issues.add(issue("DELEGATION", "DELEGATION_SELF", "ERROR", "代理人為本人",
						value.getDelegationId(), accountLabel(accountLabels, value.getPrincipalAccount()),
						"被代理人與代理人相同。", "代理設定沒有實際交接對象。",
						scopeLabel(value), "改設其他有效代理人或停用此設定。",
						"FM_PROG003D0002", List.of(accountLabel(accountLabels, value.getPrincipalAccount()))));
			}
			edges.put(value.getPrincipalAccount(), value.getDelegateAccount());
		}
		for (int left = 0; left < values.size(); left++) {
			for (int right = left + 1; right < values.size(); right++) {
				FmWorkflowDelegation first = values.get(left);
				FmWorkflowDelegation second = values.get(right);
				if (first.getPrincipalAccount().equals(second.getDelegateAccount())
						&& first.getDelegateAccount().equals(second.getPrincipalAccount())
						&& overlaps(first, second) && scopesOverlap(first, second)) {
					issues.add(issue("DELEGATION", "RECIPROCAL_DELEGATION", "WARNING",
							"同時段雙向互相代理", first.getPrincipalAccount() + "|" + first.getDelegateAccount(),
							accountLabel(accountLabels, first.getPrincipalAccount()) + " ↔ "
									+ accountLabel(accountLabels, first.getDelegateAccount()),
							"兩人於重疊期間及範圍互相代理。", "工作可能在代理鏈中循環。",
							"第一筆：「" + scopeLabel(first) + "」；第二筆：「" + scopeLabel(second) + "」。",
							"停用其中一筆或調整期間／範圍。", "FM_PROG003D0002",
							List.of(accountLabel(accountLabels, first.getPrincipalAccount()),
									accountLabel(accountLabels, first.getDelegateAccount()))));
				}
			}
		}
		issues.addAll(cycleIssues(edges, accountLabels, "DELEGATION", "DELEGATION_CYCLE",
				"代理鏈形成循環", "FM_PROG003D0002"));
		return issues;
	}

	private List<FmOrganizationHealthIssueView> cycleIssues(Map<String, String> edges,
			Map<String, String> labels, String section, String code, String title, String programId) {
		List<FmOrganizationHealthIssueView> issues = new ArrayList<>();
		Set<String> reported = new HashSet<>();
		for (String start : edges.keySet()) {
			List<String> path = new ArrayList<>();
			Map<String, Integer> positions = new HashMap<>();
			String current = start;
			while (current != null && !positions.containsKey(current)) {
				positions.put(current, path.size());
				path.add(current);
				current = edges.get(current);
			}
			if (current != null) {
				List<String> cycle = new ArrayList<>(path.subList(positions.get(current), path.size()));
				cycle.add(current);
				String key = cycle.stream().distinct().sorted().collect(Collectors.joining("|"));
				if (cycle.size() > 2 && reported.add(key)) {
					List<String> displayPath = cycle.stream()
							.map(value -> labels.getOrDefault(value, value)).toList();
					issues.add(issue(section, code, "ERROR", title, key, String.join(" → ", displayPath),
							"關聯鏈無法正常終止。", "簽核人或組織路徑解析可能失敗。",
							String.join(" → ", displayPath), "核對並修正循環中的至少一筆關聯。",
							programId, displayPath));
				}
			}
		}
		return issues;
	}

	private String managerAssignment(FmEmployeeOrgAssignment value,
			List<FmEmployeeOrgAssignment> assignments, Map<String, FmEmployeeOrgAssignment> byId,
			Map<String, FmOrgUnitView> unitsById, Map<String, String> headByUnit) {
		if ("EXPLICIT".equals(value.getManagerSource())) {
			return byId.containsKey(value.getDirectManagerAssignmentId())
					? value.getDirectManagerAssignmentId() : null;
		}
		if ("ORG_HEAD".equals(value.getManagerSource())
				|| "PARENT_HEAD".equals(value.getManagerSource())) {
			String managerUnitId = value.getOrgUnitId();
			if ("PARENT_HEAD".equals(value.getManagerSource())) {
				FmOrgUnitView unit = unitsById.get(value.getOrgUnitId());
				managerUnitId = unit == null ? null : unit.getParentOrgUnitId();
			}
			String employeeId = headByUnit.get(managerUnitId);
			String finalManagerUnitId = managerUnitId;
			return assignments.stream().filter(item -> item.getEmployeeId().equals(employeeId)
					&& item.getOrgUnitId().equals(finalManagerUnitId)).map(
						FmEmployeeOrgAssignment::getEmployeeOrgAssignmentId).findFirst().orElse(null);
		}
		return null;
	}

	private String assignmentLabel(FmEmployeeOrgAssignment assignment,
			Map<String, FmEmployee> employeesById, Map<String, FmOrgUnitView> unitsById) {
		FmEmployee employee = employeesById.get(assignment.getEmployeeId());
		String employeeLabel = employeeLabel(employee, assignment.getEmployeeId());
		String unitLabel = unitsById.containsKey(assignment.getOrgUnitId())
				? label(unitsById.get(assignment.getOrgUnitId())) : "未知部門";
		return employeeLabel + "／" + unitLabel;
	}

	private String managerSourceLabel(String managerSource) {
		return switch (StringUtils.defaultString(managerSource)) {
			case "ORG_HEAD" -> "本部門主管";
			case "PARENT_HEAD" -> "上級部門主管";
			case "EXPLICIT" -> "指定主管";
			case "NONE" -> "不配置主管";
			default -> "未知主管來源";
		};
	}

	private String managerRecommendation(String managerSource) {
		return switch (StringUtils.defaultString(managerSource)) {
			case "ORG_HEAD" -> "請確認此任職部門已配置有效的正式主管。";
			case "PARENT_HEAD" -> "請確認此部門已配置有效上級部門，且上級部門已有正式主管。";
			case "EXPLICIT" -> "請重新選擇同一 Tenant 內仍有效的主管任職。";
			default -> "請檢查此員工任職的直屬主管設定。";
		};
	}

	private Map<String, Object> parameters(String tenantId) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("status", "ACTIVE");
		return parameters;
	}

	private void requireMembership(String tenantId, Date now) throws ServiceException {
		Map<String, Object> parameters = parameters(tenantId);
		parameters.put("account", UserUtils.getCurrentUser().getUserId());
		List<FmTenantAccount> memberships = tenantAccountService.selectListByParams(parameters).getValue();
		if (memberships == null || memberships.stream().noneMatch(value ->
				effective(value.getEffectiveFrom(), value.getEffectiveTo(), now))) {
			throw new ServiceException("登入帳號不具有效 Tenant membership");
		}
	}

	private boolean effective(Date from, Date to, Date now) {
		return (from == null || !from.after(now)) && (to == null || to.after(now));
	}

	private boolean overlaps(FmWorkflowDelegation left, FmWorkflowDelegation right) {
		return (left.getEffectiveTo() == null || right.getEffectiveFrom().before(left.getEffectiveTo()))
				&& (right.getEffectiveTo() == null || left.getEffectiveFrom().before(right.getEffectiveTo()));
	}

	private boolean scopesOverlap(FmWorkflowDelegation left, FmWorkflowDelegation right) {
		return "ALL".equals(left.getScopeType()) || "ALL".equals(right.getScopeType())
				|| left.getScopeType().equals(right.getScopeType())
				&& StringUtils.equals(left.getScopeRefId(), right.getScopeRefId());
	}

	private String scope(FmWorkflowDelegation value) {
		return value.getScopeType() + (StringUtils.isBlank(value.getScopeRefId())
				? "" : ":" + value.getScopeRefId());
	}

	private String scopeLabel(FmWorkflowDelegation value) {
		String type = switch (StringUtils.defaultString(value.getScopeType())) {
			case "ALL" -> "全部流程";
			case "PROCESS" -> "指定流程";
			case "APPROVAL_GROUP" -> "指定簽核群組";
			default -> "未識別範圍";
		};
		return type + (StringUtils.isBlank(value.getScopeRefId())
				? "" : "（識別碼：" + value.getScopeRefId() + "）");
	}

	private String employeeLabel(FmEmployee employee, String fallback) {
		if (employee == null) {
			return "未知員工（識別碼：" + fallback + "）";
		}
		return employee.getDisplayName() + "（員編：" + employee.getEmployeeNo() + "）";
	}

	private String accountLabel(Map<String, String> accountLabels, String account) {
		return accountLabels.getOrDefault(account, "帳號：" + account);
	}

	private String label(FmOrgUnitView value) {
		return value.getUnitCode() + "／" + value.getUnitName();
	}

	private FmOrganizationHealthIssueView issue(String section, String code, String severity,
			String title, String subjectId, String subjectLabel, String description, String impact,
			String evidence, String recommendation, String targetProgramId, List<String> path) {
		return new FmOrganizationHealthIssueView(section, code, severity, title, subjectId,
				subjectLabel, description, impact, evidence, recommendation, targetProgramId, path);
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}

}
