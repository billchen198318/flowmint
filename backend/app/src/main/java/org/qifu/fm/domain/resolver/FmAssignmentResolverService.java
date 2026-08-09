package org.qifu.fm.domain.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.view.FmResolverCandidateView;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmAssignmentResolverService implements IFmAssignmentResolverService {

    private final IFmEmployeeService employeeService;
    private final IFmEmployeeOrgAssignmentService assignmentService;
    private final IFmOrgUnitHeadService orgUnitHeadService;

    public FmAssignmentResolverService(
            IFmEmployeeService employeeService,
            IFmEmployeeOrgAssignmentService assignmentService,
            IFmOrgUnitHeadService orgUnitHeadService) {
        this.employeeService = employeeService;
        this.assignmentService = assignmentService;
        this.orgUnitHeadService = orgUnitHeadService;
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
            case "DIRECT_MANAGER" -> directManager(rule, assignment);
            case "INITIATOR_ORG_HEAD" -> orgUnitHead(rule, assignment.getOrgUnitId());
            default -> result(rule, "UNSUPPORTED",
                    "此 Resolver 類型尚未納入第一階段預覽", List.of());
        };
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
        Map<String, Object> parameters = activeParameters(rule.getTenantId());
        parameters.put("orgUnitId", orgUnitId);
        List<FmOrgUnitHead> heads = orgUnitHeadService
                .selectListByParams(parameters, "PRIORITY", "ASC").getValue();
        if (heads.isEmpty()) {
            return result(rule, "NOT_FOUND", "申請人所屬單位沒有啟用中的主管", List.of());
        }
        FmOrgUnitHead head = heads.get(0);
        return employeeResult(rule, head.getEmployeeId(), "已解析申請人所屬單位主管");
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
                .findFirst().orElse(null);
    }

    private FmEmployee activeEmployeeById(String tenantId, String employeeId)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("employeeId", employeeId);
        return employeeService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElse(null);
    }

    private FmEmployeeOrgAssignment primaryAssignment(String tenantId, String employeeId)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("employeeId", employeeId);
        parameters.put("isPrimary", "Y");
        return assignmentService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElse(null);
    }

    private FmEmployeeOrgAssignment assignmentByBusinessId(
            String tenantId,
            String assignmentId) throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("employeeOrgAssignmentId", assignmentId);
        return assignmentService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElse(null);
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
