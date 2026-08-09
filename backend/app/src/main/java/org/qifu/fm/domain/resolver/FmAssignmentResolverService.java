package org.qifu.fm.domain.resolver;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.view.FmResolverCandidateView;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.entity.FmApprovalGroup;
import org.qifu.fm.entity.FmApprovalGroupMember;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.entity.FmOrgUnitVersion;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.service.IFmApprovalGroupMemberService;
import org.qifu.fm.service.IFmApprovalGroupService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmAssignmentResolverService implements IFmAssignmentResolverService {

    private final IFmEmployeeService employeeService;
    private final IFmEmployeeOrgAssignmentService assignmentService;
    private final IFmOrgUnitHeadService orgUnitHeadService;
    private final IFmOrgUnitVersionService orgUnitVersionService;
    private final IFmApprovalGroupService approvalGroupService;
    private final IFmApprovalGroupMemberService approvalGroupMemberService;
    private final ObjectMapper objectMapper;
    private final IFmOrgTitleService orgTitleService;
    private final IFmOrgApprovalLevelService orgApprovalLevelService;

    public FmAssignmentResolverService(
            IFmEmployeeService employeeService,
            IFmEmployeeOrgAssignmentService assignmentService,
            IFmOrgUnitHeadService orgUnitHeadService,
            IFmOrgUnitVersionService orgUnitVersionService,
            IFmApprovalGroupService approvalGroupService,
            IFmApprovalGroupMemberService approvalGroupMemberService,
            IFmOrgTitleService orgTitleService,
            IFmOrgApprovalLevelService orgApprovalLevelService,
            ObjectMapper objectMapper) {
        this.employeeService = employeeService;
        this.assignmentService = assignmentService;
        this.orgUnitHeadService = orgUnitHeadService;
        this.orgUnitVersionService = orgUnitVersionService;
        this.approvalGroupService = approvalGroupService;
        this.approvalGroupMemberService = approvalGroupMemberService;
        this.orgTitleService = orgTitleService;
        this.orgApprovalLevelService = orgApprovalLevelService;
        this.objectMapper = objectMapper;
    }

    @Override
    public FmResolverPreviewView resolve(
            FmTaskAssignmentRule rule,
            String initiatorAccount) throws ServiceException {
        if (StringUtils.isBlank(initiatorAccount)) {
            return result(rule, "ERROR", "請選擇測試申請人", List.of());
        }
        FmEmployee initiator = activeEmployeeByAccount(rule.getTenantId(), initiatorAccount);
        if (initiator == null) {
            return result(rule, "NOT_FOUND", "找不到啟用中的申請人", List.of());
        }
        FmEmployeeOrgAssignment assignment = primaryAssignment(
                rule.getTenantId(), initiator.getEmployeeId());
        if (assignment == null) {
            return result(rule, "NOT_FOUND", "申請人沒有啟用中的主要部門配置", List.of());
        }
        return switch (rule.getResolverType()) {
            case "FIXED_ACCOUNT" -> fixedAccounts(rule);
            case "APPROVAL_GROUP" -> approvalGroup(rule);
            case "DIRECT_MANAGER" -> directManager(rule, assignment);
            case "INITIATOR_ORG_HEAD" -> orgUnitHead(rule, assignment.getOrgUnitId());
            case "PARENT_ORG_HEAD" -> parentOrgUnitHead(rule, assignment.getOrgUnitId());
            case "NEXT_HIGHER_LEVEL_HEAD" -> nextHigherLevelHead(rule, assignment);
            case "TARGET_LEVEL_HEAD" -> targetLevelHead(rule, assignment);
            case "ROOT_ORG_HEAD" -> rootOrgUnitHead(rule, assignment.getOrgUnitId());
            case "MANAGER_CHAIN" -> managerChain(rule, assignment);
            case "LEVEL_HEAD_CHAIN" -> levelHeadChain(rule, assignment.getOrgUnitId());
            default -> result(rule, "UNSUPPORTED",
                    "此 Resolver 類型尚未納入第一階段預覽", List.of());
        };
    }

    private FmResolverPreviewView nextHigherLevelHead(
            FmTaskAssignmentRule rule,
            FmEmployeeOrgAssignment assignment) throws ServiceException {
        FmOrgApprovalLevel currentLevel = approvalLevel(rule.getTenantId(), assignment);
        if (currentLevel == null) {
            return result(rule, "NOT_FOUND", "申請人的職稱未配置簽核層級", List.of());
        }
        FmEmployeeOrgAssignment manager = assignment;
        Set<String> visited = new HashSet<>();
        while (StringUtils.isNotBlank(manager.getDirectManagerAssignmentId())
                && visited.add(manager.getEmployeeOrgAssignmentId())) {
            manager = assignmentByBusinessId(
                    rule.getTenantId(), manager.getDirectManagerAssignmentId());
            if (manager == null) {
                break;
            }
            FmOrgApprovalLevel managerLevel = approvalLevel(rule.getTenantId(), manager);
            if (managerLevel != null
                    && managerLevel.getLevelOrder() < currentLevel.getLevelOrder()) {
                return employeeResult(rule, manager.getEmployeeId(),
                        "已解析下一個較高簽核層級主管「" + managerLevel.getLevelName() + "」");
            }
        }
        return result(rule, "NOT_FOUND", "主管鏈中沒有更高簽核層級", List.of());
    }

    private FmResolverPreviewView targetLevelHead(
            FmTaskAssignmentRule rule,
            FmEmployeeOrgAssignment assignment) throws ServiceException {
        String targetLevelId = config(rule).path("approvalLevelId").asString();
        if (StringUtils.isBlank(targetLevelId)) {
            return result(rule, "ERROR", "尚未選擇目標簽核層級", List.of());
        }
        FmOrgApprovalLevel targetLevel = approvalLevelById(rule.getTenantId(), targetLevelId);
        if (targetLevel == null) {
            return result(rule, "NOT_FOUND", "目標簽核層級不存在或未啟用", List.of());
        }
        FmEmployeeOrgAssignment manager = assignment;
        Set<String> visited = new HashSet<>();
        while (visited.add(manager.getEmployeeOrgAssignmentId())) {
            FmOrgApprovalLevel managerLevel = approvalLevel(rule.getTenantId(), manager);
            if (managerLevel != null
                    && targetLevelId.equals(managerLevel.getApprovalLevelId())) {
                return employeeResult(rule, manager.getEmployeeId(),
                        "已解析指定簽核層級「" + targetLevel.getLevelName() + "」");
            }
            if (StringUtils.isBlank(manager.getDirectManagerAssignmentId())) {
                break;
            }
            manager = assignmentByBusinessId(
                    rule.getTenantId(), manager.getDirectManagerAssignmentId());
            if (manager == null) {
                break;
            }
        }
        return result(rule, "NOT_FOUND", "主管鏈中找不到指定簽核層級", List.of());
    }

    private FmOrgApprovalLevel approvalLevel(
            String tenantId,
            FmEmployeeOrgAssignment assignment) throws ServiceException {
        if (StringUtils.isBlank(assignment.getTitleId())) {
            return null;
        }
        Map<String, Object> titleParameters = activeParameters(tenantId);
        titleParameters.put("titleId", assignment.getTitleId());
        FmOrgTitle title = orgTitleService.selectListByParams(titleParameters).getValue().stream()
                .filter(value -> isEffective(value.getEffectiveFrom(), value.getEffectiveTo()))
                .findFirst().orElse(null);
        return title == null ? null : approvalLevelById(tenantId, title.getApprovalLevelId());
    }

    private FmOrgApprovalLevel approvalLevelById(String tenantId, String approvalLevelId)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("approvalLevelId", approvalLevelId);
        return orgApprovalLevelService.selectListByParams(parameters).getValue().stream()
                .filter(value -> isEffective(value.getEffectiveFrom(), value.getEffectiveTo()))
                .findFirst().orElse(null);
    }

    private FmResolverPreviewView fixedAccounts(FmTaskAssignmentRule rule)
            throws ServiceException {
        JsonNode accounts = config(rule).path("accounts");
        if (!accounts.isArray() || accounts.size() == 0) {
            return result(rule, "ERROR", "指定帳號規則尚未選擇帳號", List.of());
        }
        Map<String, FmResolverCandidateView> candidates = new LinkedHashMap<>();
        for (JsonNode accountNode : accounts) {
            if (candidates.size() >= rule.getMaxResults()) {
                break;
            }
            FmEmployee employee = activeEmployeeByAccount(
                    rule.getTenantId(), accountNode.asString());
            if (employee != null) {
                candidates.putIfAbsent(employee.getEmployeeId(), candidate(employee));
            }
        }
        if (candidates.isEmpty()) {
            return result(rule, "NOT_FOUND", "指定帳號均不存在或未啟用", List.of());
        }
        return result(rule, "RESOLVED", "已解析指定帳號",
                List.copyOf(candidates.values()));
    }

    private FmResolverPreviewView approvalGroup(FmTaskAssignmentRule rule)
            throws ServiceException {
        String approvalGroupId = config(rule).path("approvalGroupId").asString();
        if (StringUtils.isBlank(approvalGroupId)) {
            return result(rule, "ERROR", "簽核群組規則尚未選擇群組", List.of());
        }
        Map<String, Object> groupParameters = activeParameters(rule.getTenantId());
        groupParameters.put("approvalGroupId", approvalGroupId);
        FmApprovalGroup group = approvalGroupService.selectListByParams(groupParameters)
                .getValue().stream().findFirst().orElse(null);
        if (group == null) {
            return result(rule, "NOT_FOUND", "簽核群組不存在或未啟用", List.of());
        }
        Map<String, Object> memberParameters = activeParameters(rule.getTenantId());
        memberParameters.put("approvalGroupId", approvalGroupId);
        List<FmApprovalGroupMember> members = approvalGroupMemberService
                .selectListByParams(memberParameters, "PRIORITY", "ASC").getValue();
        Map<String, FmResolverCandidateView> candidates = new LinkedHashMap<>();
        for (FmApprovalGroupMember member : members) {
            if (candidates.size() >= rule.getMaxResults()) {
                break;
            }
            if (isEffective(member.getEffectiveFrom(), member.getEffectiveTo())) {
                addEmployeeCandidate(rule.getTenantId(), member.getEmployeeId(), candidates);
            }
        }
        if (candidates.isEmpty()) {
            return result(rule, "NOT_FOUND", "簽核群組沒有啟用中的有效成員", List.of());
        }
        return result(rule, "RESOLVED", "已解析簽核群組「" + group.getGroupName() + "」",
                List.copyOf(candidates.values()));
    }

    private JsonNode config(FmTaskAssignmentRule rule) throws ServiceException {
        try {
            return objectMapper.readTree(StringUtils.defaultIfBlank(
                    rule.getResolverConfig(), "{}"));
        } catch (RuntimeException exception) {
            throw new ServiceException("Resolver 參數不是有效的 JSON");
        }
    }

    private FmResolverPreviewView parentOrgUnitHead(
            FmTaskAssignmentRule rule,
            String orgUnitId) throws ServiceException {
        FmOrgUnitVersion unit = currentOrgUnit(rule.getTenantId(), orgUnitId);
        if (unit == null || StringUtils.isBlank(unit.getParentOrgUnitId())) {
            return result(rule, "NOT_FOUND", "申請人所屬單位沒有上一層單位", List.of());
        }
        return orgUnitHead(rule, unit.getParentOrgUnitId());
    }

    private FmResolverPreviewView rootOrgUnitHead(
            FmTaskAssignmentRule rule,
            String orgUnitId) throws ServiceException {
        String currentId = orgUnitId;
        Set<String> visited = new HashSet<>();
        while (visited.add(currentId)) {
            FmOrgUnitVersion unit = currentOrgUnit(rule.getTenantId(), currentId);
            if (unit == null || StringUtils.isBlank(unit.getParentOrgUnitId())) {
                return orgUnitHead(rule, currentId);
            }
            currentId = unit.getParentOrgUnitId();
        }
        return result(rule, "ERROR", "組織階層存在循環，無法解析最高層主管", List.of());
    }

    private FmResolverPreviewView managerChain(
            FmTaskAssignmentRule rule,
            FmEmployeeOrgAssignment assignment) throws ServiceException {
        Map<String, FmResolverCandidateView> candidates = new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();
        FmEmployeeOrgAssignment current = assignment;
        while (StringUtils.isNotBlank(current.getDirectManagerAssignmentId())
                && visited.add(current.getEmployeeOrgAssignmentId())
                && candidates.size() < rule.getMaxResults()) {
            current = assignmentByBusinessId(
                    rule.getTenantId(), current.getDirectManagerAssignmentId());
            if (current == null) {
                break;
            }
            addEmployeeCandidate(rule.getTenantId(), current.getEmployeeId(), candidates);
        }
        if (candidates.isEmpty()) {
            return result(rule, "NOT_FOUND", "找不到任何逐級直屬主管", List.of());
        }
        return result(rule, "RESOLVED", "已依直屬主管配置逐級解析",
                List.copyOf(candidates.values()));
    }

    private FmResolverPreviewView levelHeadChain(
            FmTaskAssignmentRule rule,
            String orgUnitId) throws ServiceException {
        Map<String, FmResolverCandidateView> candidates = new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();
        String currentId = orgUnitId;
        while (StringUtils.isNotBlank(currentId) && visited.add(currentId)
                && candidates.size() < rule.getMaxResults()) {
            FmOrgUnitHead head = firstOrgUnitHead(rule.getTenantId(), currentId);
            if (head != null) {
                addEmployeeCandidate(rule.getTenantId(), head.getEmployeeId(), candidates);
            }
            FmOrgUnitVersion unit = currentOrgUnit(rule.getTenantId(), currentId);
            currentId = unit == null ? null : unit.getParentOrgUnitId();
        }
        if (candidates.isEmpty()) {
            return result(rule, "NOT_FOUND", "組織鏈上沒有啟用中的單位主管", List.of());
        }
        return result(rule, "RESOLVED", "已由所屬單位向上逐級解析主管",
                List.copyOf(candidates.values()));
    }

    private FmResolverPreviewView directManager(
            FmTaskAssignmentRule rule,
            FmEmployeeOrgAssignment assignment) throws ServiceException {
        if (StringUtils.isBlank(assignment.getDirectManagerAssignmentId())) {
            return result(rule, "NOT_FOUND", "主要部門配置沒有直屬主管", List.of());
        }
        FmEmployeeOrgAssignment managerAssignment = assignmentByBusinessId(
                rule.getTenantId(), assignment.getDirectManagerAssignmentId());
        if (managerAssignment == null) {
            return result(rule, "NOT_FOUND", "直屬主管的部門配置不存在或未啟用", List.of());
        }
        return employeeResult(rule, managerAssignment.getEmployeeId(), "已解析直屬主管");
    }

    private FmResolverPreviewView orgUnitHead(
            FmTaskAssignmentRule rule,
            String orgUnitId) throws ServiceException {
        FmOrgUnitHead head = firstOrgUnitHead(rule.getTenantId(), orgUnitId);
        if (head == null) {
            return result(rule, "NOT_FOUND", "申請人所屬單位沒有啟用中的主管", List.of());
        }
        return employeeResult(rule, head.getEmployeeId(), "已解析申請人所屬單位主管");
    }

    private FmOrgUnitHead firstOrgUnitHead(String tenantId, String orgUnitId)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("orgUnitId", orgUnitId);
        return orgUnitHeadService.selectListByParams(parameters, "PRIORITY", "ASC")
                .getValue().stream()
                .filter(head -> isEffective(head.getEffectiveFrom(), head.getEffectiveTo()))
                .findFirst().orElse(null);
    }

    private FmOrgUnitVersion currentOrgUnit(String tenantId, String orgUnitId)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("orgUnitId", orgUnitId);
        return orgUnitVersionService.selectListByParams(parameters, "VERSION_NO", "DESC")
                .getValue().stream()
                .filter(unit -> isEffective(unit.getEffectiveFrom(), unit.getEffectiveTo()))
                .findFirst().orElse(null);
    }

    private void addEmployeeCandidate(
            String tenantId,
            String employeeId,
            Map<String, FmResolverCandidateView> candidates) throws ServiceException {
        FmEmployee employee = activeEmployeeById(tenantId, employeeId);
        if (employee != null) {
            candidates.putIfAbsent(employee.getEmployeeId(), candidate(employee));
        }
    }

    private FmResolverCandidateView candidate(FmEmployee employee) {
        return new FmResolverCandidateView(
                employee.getEmployeeId(), employee.getAccount(), employee.getDisplayName());
    }

    private FmResolverPreviewView employeeResult(
            FmTaskAssignmentRule rule,
            String employeeId,
            String message) throws ServiceException {
        FmEmployee employee = activeEmployeeById(rule.getTenantId(), employeeId);
        if (employee == null) {
            return result(rule, "NOT_FOUND", "主管員工不存在或未啟用", List.of());
        }
        FmResolverCandidateView candidate = new FmResolverCandidateView(
                employee.getEmployeeId(), employee.getAccount(), employee.getDisplayName());
        return result(rule, "RESOLVED", message, List.of(candidate));
    }

    private FmEmployee activeEmployeeByAccount(String tenantId, String account)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("account", account);
        return employeeService.selectListByParams(parameters).getValue().stream()
                .filter(employee -> isEffective(
                        employee.getEffectiveFrom(), employee.getEffectiveTo()))
                .findFirst().orElse(null);
    }

    private FmEmployee activeEmployeeById(String tenantId, String employeeId)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("employeeId", employeeId);
        return employeeService.selectListByParams(parameters).getValue().stream()
                .filter(employee -> isEffective(
                        employee.getEffectiveFrom(), employee.getEffectiveTo()))
                .findFirst().orElse(null);
    }

    private FmEmployeeOrgAssignment primaryAssignment(String tenantId, String employeeId)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("employeeId", employeeId);
        parameters.put("isPrimary", "Y");
        return assignmentService.selectListByParams(parameters).getValue().stream()
                .filter(assignment -> isEffective(
                        assignment.getEffectiveFrom(), assignment.getEffectiveTo()))
                .findFirst().orElse(null);
    }

    private FmEmployeeOrgAssignment assignmentByBusinessId(
            String tenantId,
            String assignmentId) throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("employeeOrgAssignmentId", assignmentId);
        return assignmentService.selectListByParams(parameters).getValue().stream()
                .filter(assignment -> isEffective(
                        assignment.getEffectiveFrom(), assignment.getEffectiveTo()))
                .findFirst().orElse(null);
    }

    private boolean isEffective(Date effectiveFrom, Date effectiveTo) {
        Date now = new Date();
        return (effectiveFrom == null || !effectiveFrom.after(now))
                && (effectiveTo == null || effectiveTo.after(now));
    }

    private Map<String, Object> activeParameters(String tenantId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        return parameters;
    }

    private FmResolverPreviewView result(
            FmTaskAssignmentRule rule,
            String status,
            String message,
            List<FmResolverCandidateView> candidates) {
        return new FmResolverPreviewView(rule.getTaskDefKey(), rule.getRuleSeq(),
                rule.getResolverType(), status, message, candidates);
    }
}
