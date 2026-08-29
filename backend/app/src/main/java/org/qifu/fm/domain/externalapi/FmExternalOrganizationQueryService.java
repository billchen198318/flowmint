package org.qifu.fm.domain.externalapi;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.external.FmExternalDepartmentDetailRequest;
import org.qifu.fm.dto.external.FmExternalDepartmentView;
import org.qifu.fm.dto.external.FmExternalDepartmentHeadView;
import org.qifu.fm.dto.external.FmExternalDepartmentEmployeesRequest;
import org.qifu.fm.dto.external.FmExternalDepartmentEmployeesView;
import org.qifu.fm.dto.external.FmExternalDepartmentTreeRequest;
import org.qifu.fm.dto.external.FmExternalDepartmentTreeView;
import org.qifu.fm.dto.external.FmExternalEmployeeDetailRequest;
import org.qifu.fm.dto.external.FmExternalEmployeeDepartmentHeadView;
import org.qifu.fm.dto.external.FmExternalEmployeeDepartmentsView;
import org.qifu.fm.dto.external.FmExternalEmployeeApprovalLevelView;
import org.qifu.fm.dto.external.FmExternalEmployeeManagerView;
import org.qifu.fm.dto.external.FmExternalEmployeeOrganizationRequest;
import org.qifu.fm.dto.external.FmExternalEmployeeParentDepartmentView;
import org.qifu.fm.dto.external.FmExternalEmployeeView;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmExternalOrganizationQueryService {
	private static final int MAX_PAGE_SIZE = 200;
	private static final int MAX_TREE_NODES = 2000;

	private final IFmOrgUnitVersionService orgUnitVersionService;
	private final IFmOrgUnitHeadService orgUnitHeadService;
	private final IFmEmployeeService employeeService;
	private final IFmEmployeeOrgAssignmentService assignmentService;
	private final IFmOrgTitleService titleService;
	private final IFmOrgApprovalLevelService approvalLevelService;

	public FmExternalOrganizationQueryService(
			IFmOrgUnitVersionService orgUnitVersionService,
			IFmOrgUnitHeadService orgUnitHeadService,
			IFmEmployeeService employeeService,
			IFmEmployeeOrgAssignmentService assignmentService,
			IFmOrgTitleService titleService,
			IFmOrgApprovalLevelService approvalLevelService) {
		this.orgUnitVersionService = orgUnitVersionService;
		this.orgUnitHeadService = orgUnitHeadService;
		this.employeeService = employeeService;
		this.assignmentService = assignmentService;
		this.titleService = titleService;
		this.approvalLevelService = approvalLevelService;
	}

	public FmExternalDepartmentHeadView primaryDepartmentHead(
			FmExternalDepartmentDetailRequest request) throws ServiceException {
		FmExternalDepartmentView department = departmentDetail(request);
		if (department == null) {
			return null;
		}
		FmExternalApiPrincipal principal = FmExternalApiContext.getRequired();
		Date effectiveAt = effectiveDate(request.effectiveAt());
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", principal.tenantId());
		parameters.put("orgUnitId", request.orgUnitId().trim());
		parameters.put("headType", "PRIMARY");
		parameters.put("status", "ACTIVE");
		FmOrgUnitHead head = orgUnitHeadService.selectListByParams(parameters).getValue()
				.stream().filter(value -> effective(value.getEffectiveFrom(),
						value.getEffectiveTo(), effectiveAt))
				.sorted(Comparator.comparing(FmOrgUnitHead::getPriority,
						Comparator.nullsLast(Integer::compareTo)))
				.findFirst().orElse(null);
		if (head == null) {
			return new FmExternalDepartmentHeadView(department, null,
					"PRIMARY_HEAD_NOT_CONFIGURED");
		}
		FmEmployee employee = employeeById(principal.tenantId(), head.getEmployeeId(),
				effectiveAt);
		if (employee == null) {
			return new FmExternalDepartmentHeadView(department, null,
					"PRIMARY_HEAD_EMPLOYEE_NOT_ACTIVE");
		}
		return new FmExternalDepartmentHeadView(department,
				new FmExternalDepartmentHeadView.Head(employee.getAccount(),
						employee.getEmployeeId(), employee.getDisplayName(), head.getHeadType(),
						head.getEffectiveFrom(), head.getEffectiveTo()), null);
	}

	public FmExternalDepartmentEmployeesView departmentEmployees(
			FmExternalDepartmentEmployeesRequest request) throws ServiceException {
		if (request == null || StringUtils.isBlank(request.orgUnitId())) {
			throw new ServiceException("orgUnitId is required.");
		}
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"org.department.read");
		Date at = effectiveDate(request.effectiveAt());
		FmExternalDepartmentView requestedDepartment = department(principal.tenantId(),
				request.orgUnitId().trim(), at);
		if (requestedDepartment == null) {
			return null;
		}
		Set<String> departmentIds = new LinkedHashSet<>();
		departmentIds.add(requestedDepartment.orgUnitId());
		if (Boolean.TRUE.equals(request.includeSubtree())) {
			List<FmOrgUnitView> units = orgUnitVersionService.selectEffectiveTree(
					principal.tenantId(), at, false);
			boolean changed;
			do {
				changed = false;
				for (FmOrgUnitView unit : units) {
					if (departmentIds.contains(unit.getParentOrgUnitId())) {
						changed |= departmentIds.add(unit.getOrgUnitId());
					}
				}
			} while (changed);
		}
		String status = StringUtils.defaultIfBlank(request.status(), "ACTIVE")
				.trim().toUpperCase(java.util.Locale.ROOT);
		if (!Set.of("ACTIVE", "INACTIVE").contains(status)) {
			throw new ServiceException("status must be ACTIVE or INACTIVE.");
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", principal.tenantId());
		parameters.put("status", status);
		boolean primaryOnly = request.primaryOnly() == null
				|| Boolean.TRUE.equals(request.primaryOnly());
		List<FmEmployeeOrgAssignment> assignments = assignmentService
				.selectListByParams(parameters).getValue().stream()
				.filter(value -> departmentIds.contains(value.getOrgUnitId()))
				.filter(value -> !primaryOnly || "Y".equals(value.getIsPrimary()))
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(), at))
				.sorted(Comparator.comparing(FmEmployeeOrgAssignment::getEmployeeId)
						.thenComparing(FmEmployeeOrgAssignment::getEmployeeOrgAssignmentId))
				.toList();
		List<FmExternalDepartmentEmployeesView.Employee> employees = new ArrayList<>();
		for (FmEmployeeOrgAssignment assignment : assignments) {
			FmEmployee employee = employeeById(principal.tenantId(),
					assignment.getEmployeeId(), at);
			if (employee != null) {
				employees.add(departmentEmployee(employee, assignment, at));
			}
		}
		int page = positive(request.page(), 1, "page");
		int pageSize = boundedPageSize(request.pageSize());
		int from = Math.min((page - 1) * pageSize, employees.size());
		int to = Math.min(from + pageSize, employees.size());
		int pages = employees.isEmpty() ? 0
				: (employees.size() + pageSize - 1) / pageSize;
		return new FmExternalDepartmentEmployeesView(requestedDepartment,
				employees.size(), pages, page, pageSize, List.copyOf(employees.subList(from, to)));
	}

	public FmExternalDepartmentTreeView departmentTree(
			FmExternalDepartmentTreeRequest request) throws ServiceException {
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"org.department.read");
		FmExternalDepartmentTreeRequest value = request == null
				? new FmExternalDepartmentTreeRequest(null, null, false, "TREE", 1, 50)
				: request;
		Date at = effectiveDate(value.effectiveAt());
		String format = StringUtils.defaultIfBlank(value.format(), "TREE")
				.trim().toUpperCase(java.util.Locale.ROOT);
		if (!Set.of("TREE", "FLAT").contains(format)) {
			throw new ServiceException("format must be TREE or FLAT.");
		}
		List<FmOrgUnitView> units = orgUnitVersionService.selectEffectiveTree(
				principal.tenantId(), at, Boolean.TRUE.equals(value.includeInactive()));
		if (StringUtils.isNotBlank(value.rootOrgUnitId())) {
			String rootId = value.rootOrgUnitId().trim();
			if (units.stream().noneMatch(unit -> rootId.equals(unit.getOrgUnitId()))) {
				return null;
			}
			Set<String> included = new LinkedHashSet<>();
			included.add(rootId);
			boolean changed;
			do {
				changed = false;
				for (FmOrgUnitView unit : units) {
					if (included.contains(unit.getParentOrgUnitId())) {
						changed |= included.add(unit.getOrgUnitId());
					}
				}
			} while (changed);
			units = units.stream().filter(unit -> included.contains(unit.getOrgUnitId()))
					.toList();
		}
		List<FmExternalDepartmentView> views = units.stream().map(this::departmentView)
				.toList();
		if ("TREE".equals(format)) {
			if (views.size() > MAX_TREE_NODES) {
				throw new ServiceException("TREE result exceeds 2000 nodes; use FLAT pagination.");
			}
			return new FmExternalDepartmentTreeView(format, views.size(), 1, 1,
					views.size(), treeNodes(views, value.rootOrgUnitId()), List.of());
		}
		int page = positive(value.page(), 1, "page");
		int pageSize = boundedPageSize(value.pageSize());
		int from = Math.min((page - 1) * pageSize, views.size());
		int to = Math.min(from + pageSize, views.size());
		int pages = views.isEmpty() ? 0 : (views.size() + pageSize - 1) / pageSize;
		return new FmExternalDepartmentTreeView(format, views.size(), pages, page,
				pageSize, List.of(), List.copyOf(views.subList(from, to)));
	}

	private List<FmExternalDepartmentTreeView.Node> treeNodes(
			List<FmExternalDepartmentView> departments, String rootOrgUnitId) {
		Map<String, List<FmExternalDepartmentView>> children = new LinkedHashMap<>();
		for (FmExternalDepartmentView department : departments) {
			children.computeIfAbsent(department.parentOrgUnitId(), key -> new ArrayList<>())
					.add(department);
		}
		if (StringUtils.isNotBlank(rootOrgUnitId)) {
			FmExternalDepartmentView root = departments.stream()
					.filter(value -> rootOrgUnitId.trim().equals(value.orgUnitId()))
					.findFirst().orElseThrow();
			return List.of(treeNode(root, children));
		}
		Set<String> ids = departments.stream().map(FmExternalDepartmentView::orgUnitId)
				.collect(java.util.stream.Collectors.toSet());
		return departments.stream()
				.filter(value -> StringUtils.isBlank(value.parentOrgUnitId())
						|| !ids.contains(value.parentOrgUnitId()))
				.map(value -> treeNode(value, children)).toList();
	}

	private FmExternalDepartmentTreeView.Node treeNode(FmExternalDepartmentView value,
			Map<String, List<FmExternalDepartmentView>> children) {
		return new FmExternalDepartmentTreeView.Node(value,
				children.getOrDefault(value.orgUnitId(), List.of()).stream()
						.map(child -> treeNode(child, children)).toList());
	}

	private FmExternalDepartmentEmployeesView.Employee departmentEmployee(
			FmEmployee employee, FmEmployeeOrgAssignment assignment, Date at)
			throws ServiceException {
		FmOrgTitle title = activeTitle(assignment.getTitleId(), at);
		FmOrgApprovalLevel level = title == null ? null
				: activeApprovalLevel(title.getApprovalLevelId(), at);
		return new FmExternalDepartmentEmployeesView.Employee(employee.getAccount(),
				employee.getEmployeeId(), employee.getDisplayName(), assignment.getOrgUnitId(),
				assignment.getTitleId(), title == null ? null : title.getTitleName(),
				level == null ? null : level.getApprovalLevelId(),
				level == null ? null : level.getLevelCode(),
				level == null ? null : level.getLevelName(),
				level == null ? null : level.getLevelOrder(), assignment.getIsPrimary(),
				assignment.getEffectiveFrom(), assignment.getEffectiveTo());
	}

	private FmOrgApprovalLevel activeApprovalLevel(String approvalLevelId, Date at)
			throws ServiceException {
		if (StringUtils.isBlank(approvalLevelId)) {
			return null;
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", principal().tenantId());
		parameters.put("approvalLevelId", approvalLevelId);
		parameters.put("status", "ACTIVE");
		return approvalLevelService.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(), at))
				.findFirst().orElse(null);
	}

	private int positive(Integer value, int defaultValue, String field)
			throws ServiceException {
		int result = value == null ? defaultValue : value;
		if (result < 1) {
			throw new ServiceException(field + " must be greater than zero.");
		}
		return result;
	}

	private int boundedPageSize(Integer value) throws ServiceException {
		int result = positive(value, 50, "pageSize");
		if (result > MAX_PAGE_SIZE) {
			throw new ServiceException("pageSize must not exceed 200.");
		}
		return result;
	}

	public FmExternalEmployeeView employeeDetail(FmExternalEmployeeDetailRequest request)
			throws ServiceException {
		if (request == null || StringUtils.isBlank(request.account())) {
			throw new ServiceException("account is required.");
		}
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"org.employee.read");
		Date effectiveAt = effectiveDate(request.effectiveAt());
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", principal.tenantId());
		parameters.put("account", request.account().trim());
		FmEmployee employee = employeeService.selectListByParams(parameters).getValue()
				.stream().filter(value -> effective(value.getEffectiveFrom(),
						value.getEffectiveTo(), effectiveAt))
				.findFirst().orElse(null);
		if (employee == null) {
			return null;
		}
		FmExternalDepartmentView department = primaryDepartment(principal.tenantId(),
				employee.getEmployeeId(), effectiveAt);
		return new FmExternalEmployeeView(employee.getAccount(), employee.getEmployeeId(),
				employee.getEmployeeNo(), employee.getDisplayName(), employee.getStatus(),
				employee.getEffectiveFrom(), employee.getEffectiveTo(), employee.getEmail(),
				department);
	}

	public FmExternalEmployeeDepartmentsView employeeDepartments(
			FmExternalEmployeeOrganizationRequest request) throws ServiceException {
		EmployeeContext context = employeeContext(request);
		if (context == null) {
			return null;
		}
		boolean primaryOnly = Boolean.TRUE.equals(request.primaryOnly());
		List<FmExternalEmployeeDepartmentsView.Assignment> assignments = context.assignments()
				.stream().filter(value -> !primaryOnly || "Y".equals(value.getIsPrimary()))
				.map(value -> new FmExternalEmployeeDepartmentsView.Assignment(
						value.getEmployeeOrgAssignmentId(),
						department(principal().tenantId(), value.getOrgUnitId(),
								context.effectiveAt()), value.getTitleId(), value.getIsPrimary(),
						value.getEffectiveFrom(), value.getEffectiveTo()))
				.toList();
		return new FmExternalEmployeeDepartmentsView(employeeSummary(context.employee()),
				assignments);
	}

	public FmExternalEmployeeDepartmentHeadView employeePrimaryDepartmentHead(
			FmExternalEmployeeOrganizationRequest request) throws ServiceException {
		EmployeeContext context = employeeContext(request);
		if (context == null) {
			return null;
		}
		FmEmployeeOrgAssignment primary = uniquePrimary(context.assignments());
		FmExternalDepartmentView department = department(principal().tenantId(),
				primary.getOrgUnitId(), context.effectiveAt());
		FmExternalDepartmentHeadView departmentHead = primaryDepartmentHead(
				new FmExternalDepartmentDetailRequest(primary.getOrgUnitId(),
						request.effectiveAt()));
		return new FmExternalEmployeeDepartmentHeadView(employeeSummary(context.employee()),
				department, departmentHead == null ? null : departmentHead.head(),
				departmentHead == null ? "ORG_UNIT_NOT_FOUND" : departmentHead.warning());
	}

	public FmExternalEmployeeParentDepartmentView employeeParentDepartment(
			FmExternalEmployeeOrganizationRequest request) throws ServiceException {
		EmployeeContext context = employeeContext(request);
		if (context == null) {
			return null;
		}
		FmEmployeeOrgAssignment primary = uniquePrimary(context.assignments());
		FmExternalDepartmentView department = department(principal().tenantId(),
				primary.getOrgUnitId(), context.effectiveAt());
		List<FmExternalDepartmentView> ancestors = new java.util.ArrayList<>();
		String parentId = department == null ? null : department.parentOrgUnitId();
		while (StringUtils.isNotBlank(parentId)) {
			FmExternalDepartmentView parent = department(principal().tenantId(), parentId,
					context.effectiveAt());
			if (parent == null) {
				break;
			}
			ancestors.add(parent);
			if (!Boolean.TRUE.equals(request.includeAncestors())) {
				break;
			}
			parentId = parent.parentOrgUnitId();
		}
		return new FmExternalEmployeeParentDepartmentView(employeeSummary(context.employee()),
				department, ancestors.isEmpty() ? null : ancestors.get(0),
				Boolean.TRUE.equals(request.includeAncestors()) ? List.copyOf(ancestors)
						: List.of());
	}

	public FmExternalEmployeeApprovalLevelView employeeApprovalLevel(
			FmExternalEmployeeOrganizationRequest request) throws ServiceException {
		EmployeeContext context = employeeContext(request);
		if (context == null) {
			return null;
		}
		FmEmployeeOrgAssignment assignment = selectedAssignment(context.assignments(),
				request.orgUnitId());
		FmExternalEmployeeDepartmentsView.Assignment assignmentView = assignmentView(
				assignment, context.effectiveAt());
		FmOrgTitle title = activeTitle(assignment.getTitleId(), context.effectiveAt());
		if (title == null || StringUtils.isBlank(title.getApprovalLevelId())) {
			return new FmExternalEmployeeApprovalLevelView(employeeSummary(context.employee()),
					assignmentView, null);
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", principal().tenantId());
		parameters.put("approvalLevelId", title.getApprovalLevelId());
		parameters.put("status", "ACTIVE");
		FmOrgApprovalLevel level = approvalLevelService.selectListByParams(parameters)
				.getValue().stream().filter(value -> effective(value.getEffectiveFrom(),
						value.getEffectiveTo(), context.effectiveAt())).findFirst().orElse(null);
		FmExternalEmployeeApprovalLevelView.Level levelView = level == null ? null
				: new FmExternalEmployeeApprovalLevelView.Level(level.getLevelSchemeId(),
						level.getApprovalLevelId(), level.getLevelCode(), level.getLevelName(),
						level.getLevelOrder());
		return new FmExternalEmployeeApprovalLevelView(employeeSummary(context.employee()),
				assignmentView, levelView);
	}

	public FmExternalEmployeeManagerView employeeDirectManager(
			FmExternalEmployeeOrganizationRequest request) throws ServiceException {
		EmployeeContext context = employeeContext(request);
		if (context == null) {
			return null;
		}
		FmEmployeeOrgAssignment assignment = selectedAssignment(context.assignments(),
				request.orgUnitId());
		FmExternalEmployeeManagerView.Manager manager = explicitManager(assignment,
				context.effectiveAt());
		String warning = null;
		if (manager == null && Boolean.TRUE.equals(request.fallbackToOrgHead())) {
			FmExternalDepartmentHeadView head = primaryDepartmentHead(
					new FmExternalDepartmentDetailRequest(assignment.getOrgUnitId(),
							request.effectiveAt()));
			if (head != null && head.head() != null) {
				manager = new FmExternalEmployeeManagerView.Manager(head.head().account(),
						head.head().employeeId(), head.head().displayName(), null,
						"ORG_PRIMARY_HEAD");
			} else {
				warning = head == null ? "ORG_UNIT_NOT_FOUND" : head.warning();
			}
		} else if (manager == null) {
			warning = "DIRECT_MANAGER_NOT_CONFIGURED";
		}
		return new FmExternalEmployeeManagerView(employeeSummary(context.employee()),
				assignmentView(assignment, context.effectiveAt()), manager, warning);
	}

	private FmExternalEmployeeManagerView.Manager explicitManager(
			FmEmployeeOrgAssignment assignment, Date effectiveAt) throws ServiceException {
		if (!"EXPLICIT".equals(assignment.getManagerSource())
				|| StringUtils.isBlank(assignment.getDirectManagerAssignmentId())) {
			return null;
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", principal().tenantId());
		parameters.put("employeeOrgAssignmentId",
				assignment.getDirectManagerAssignmentId());
		parameters.put("status", "ACTIVE");
		FmEmployeeOrgAssignment managerAssignment = assignmentService
				.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(),
						effectiveAt)).findFirst().orElse(null);
		if (managerAssignment == null) {
			return null;
		}
		FmEmployee manager = employeeById(principal().tenantId(),
				managerAssignment.getEmployeeId(), effectiveAt);
		return manager == null ? null : new FmExternalEmployeeManagerView.Manager(
				manager.getAccount(), manager.getEmployeeId(), manager.getDisplayName(),
				managerAssignment.getEmployeeOrgAssignmentId(), "EXPLICIT");
	}

	private FmEmployeeOrgAssignment selectedAssignment(
			List<FmEmployeeOrgAssignment> assignments, String orgUnitId) {
		if (StringUtils.isBlank(orgUnitId)) {
			return uniquePrimary(assignments);
		}
		List<FmEmployeeOrgAssignment> matches = assignments.stream()
				.filter(value -> orgUnitId.trim().equals(value.getOrgUnitId())).toList();
		if (matches.size() != 1) {
			throw new FmExternalApiConflictException("ASSIGNMENT_AMBIGUOUS",
					"The employee does not have exactly one active assignment in the department.");
		}
		return matches.get(0);
	}

	private FmOrgTitle activeTitle(String titleId, Date effectiveAt)
			throws ServiceException {
		if (StringUtils.isBlank(titleId)) {
			return null;
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", principal().tenantId());
		parameters.put("titleId", titleId);
		parameters.put("status", "ACTIVE");
		return titleService.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(),
						effectiveAt)).findFirst().orElse(null);
	}

	private FmExternalEmployeeDepartmentsView.Assignment assignmentView(
			FmEmployeeOrgAssignment value, Date effectiveAt) {
		return new FmExternalEmployeeDepartmentsView.Assignment(
				value.getEmployeeOrgAssignmentId(), department(principal().tenantId(),
						value.getOrgUnitId(), effectiveAt), value.getTitleId(),
				value.getIsPrimary(), value.getEffectiveFrom(), value.getEffectiveTo());
	}

	private EmployeeContext employeeContext(FmExternalEmployeeOrganizationRequest request)
			throws ServiceException {
		if (request == null || StringUtils.isBlank(request.account())) {
			throw new ServiceException("account is required.");
		}
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"org.employee.read");
		Date at = effectiveDate(request.effectiveAt());
		Map<String, Object> employeeParameters = new HashMap<>();
		employeeParameters.put("tenantId", principal.tenantId());
		employeeParameters.put("account", request.account().trim());
		FmEmployee employee = employeeService.selectListByParams(employeeParameters).getValue()
				.stream().filter(value -> effective(value.getEffectiveFrom(),
						value.getEffectiveTo(), at)).findFirst().orElse(null);
		if (employee == null) {
			return null;
		}
		Map<String, Object> assignmentParameters = new HashMap<>();
		assignmentParameters.put("tenantId", principal.tenantId());
		assignmentParameters.put("employeeId", employee.getEmployeeId());
		assignmentParameters.put("status", "ACTIVE");
		List<FmEmployeeOrgAssignment> assignments = assignmentService
				.selectListByParams(assignmentParameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(), at))
				.toList();
		return new EmployeeContext(employee, assignments, at);
	}

	private FmEmployeeOrgAssignment uniquePrimary(List<FmEmployeeOrgAssignment> values) {
		List<FmEmployeeOrgAssignment> primary = values.stream()
				.filter(value -> "Y".equals(value.getIsPrimary())).toList();
		if (primary.size() != 1) {
			throw new FmExternalApiConflictException("PRIMARY_ASSIGNMENT_AMBIGUOUS",
					"The employee does not have exactly one active primary assignment.");
		}
		return primary.get(0);
	}

	private FmExternalEmployeeDepartmentsView.Employee employeeSummary(FmEmployee value) {
		return new FmExternalEmployeeDepartmentsView.Employee(value.getAccount(),
				value.getEmployeeId(), value.getDisplayName());
	}

	private FmExternalDepartmentView department(String tenantId, String orgUnitId,
			Date effectiveAt) {
		FmOrgUnitView unit = orgUnitVersionService.selectEffective(tenantId, orgUnitId,
				effectiveAt);
		return unit == null ? null : departmentView(unit);
	}

	private FmExternalApiPrincipal principal() {
		return FmExternalApiContext.getRequired();
	}

	private record EmployeeContext(FmEmployee employee,
			List<FmEmployeeOrgAssignment> assignments, Date effectiveAt) { }

	private FmExternalDepartmentView primaryDepartment(String tenantId,
			String employeeId, Date effectiveAt) throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("employeeId", employeeId);
		parameters.put("isPrimary", "Y");
		parameters.put("status", "ACTIVE");
		List<FmEmployeeOrgAssignment> assignments = assignmentService
				.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(),
						effectiveAt)).toList();
		if (assignments.size() != 1) {
			return null;
		}
		FmOrgUnitView unit = orgUnitVersionService.selectEffective(tenantId,
				assignments.get(0).getOrgUnitId(), effectiveAt);
		return unit == null ? null : departmentView(unit);
	}

	private FmEmployee employeeById(String tenantId, String employeeId,
			Date effectiveAt) throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("employeeId", employeeId);
		return employeeService.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value.getEffectiveFrom(), value.getEffectiveTo(),
						effectiveAt)).findFirst().orElse(null);
	}

	private Date effectiveDate(OffsetDateTime value) {
		return Date.from((value == null ? OffsetDateTime.now() : value).toInstant());
	}

	private boolean effective(Date from, Date to, Date at) {
		return (from == null || !from.after(at)) && (to == null || to.after(at));
	}

	private FmExternalDepartmentView departmentView(FmOrgUnitView unit) {
		return new FmExternalDepartmentView(unit.getOrgUnitId(), unit.getUnitCode(),
				unit.getUnitName(), unit.getShortName(), unit.getUnitType(), unit.getStatus(),
				unit.getParentOrgUnitId(), unit.getPath(), unit.getTreeDepth(),
				unit.getSortNo(), unit.getEffectiveFrom(), unit.getEffectiveTo());
	}

	public FmExternalDepartmentView departmentDetail(
			FmExternalDepartmentDetailRequest request) throws ServiceException {
		if (request == null || StringUtils.isBlank(request.orgUnitId())) {
			throw new ServiceException("orgUnitId is required.");
		}
		FmExternalApiPrincipal principal = FmExternalApiContext.requireScope(
				"org.department.read");
		OffsetDateTime effectiveAt = request.effectiveAt() == null
				? OffsetDateTime.now() : request.effectiveAt();
		FmOrgUnitView unit = orgUnitVersionService.selectEffective(
				principal.tenantId(), request.orgUnitId().trim(),
				Date.from(effectiveAt.toInstant()));
		if (unit == null) {
			return null;
		}
		return new FmExternalDepartmentView(unit.getOrgUnitId(), unit.getUnitCode(),
				unit.getUnitName(), unit.getShortName(), unit.getUnitType(),
				unit.getStatus(), unit.getParentOrgUnitId(), unit.getPath(),
				unit.getTreeDepth(), unit.getSortNo(), unit.getEffectiveFrom(),
				unit.getEffectiveTo());
	}
}
