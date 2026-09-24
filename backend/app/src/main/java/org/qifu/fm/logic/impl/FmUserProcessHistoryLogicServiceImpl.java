package org.qifu.fm.logic.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmUserProcessHistoryRequest;
import org.qifu.fm.dto.view.FmRequestProcessDiagramView;
import org.qifu.fm.dto.view.FmRequestTrackDetailView;
import org.qifu.fm.dto.view.FmUserProcessHistoryPageView;
import org.qifu.fm.dto.view.FmUserProcessHistoryView;
import org.qifu.fm.logic.IFmRequestTrackingLogicService;
import org.qifu.fm.logic.IFmUserProcessHistoryLogicService;
import org.qifu.fm.model.FmUserProcessHistoryRow;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmUserProcessHistoryLogicServiceImpl implements IFmUserProcessHistoryLogicService {
    private static final List<String> RELATIONS = List.of("ALL", "STARTED", "SIGNED");
    private static final List<String> STATUSES = List.of(
            "RUNNING", "COMPLETED", "REJECTED", "CANCELLED", "TERMINATED");
    private static final List<Integer> PAGE_SIZES = List.of(10, 20, 30, 50, 100);

    private final FmTenantAccessGuard tenantAccessGuard;
    private final IFmProcessInstanceService processInstanceService;
    private final IFmRequestTrackingLogicService trackingLogicService;
    private final TaskService taskService;

    public FmUserProcessHistoryLogicServiceImpl(
            FmTenantAccessGuard tenantAccessGuard,
            IFmProcessInstanceService processInstanceService,
            IFmRequestTrackingLogicService trackingLogicService,
            TaskService taskService) {
        this.tenantAccessGuard = tenantAccessGuard;
        this.processInstanceService = processInstanceService;
        this.trackingLogicService = trackingLogicService;
        this.taskService = taskService;
    }

    @Override
    public DefaultResult<FmUserProcessHistoryPageView> findPage(
            String tenantId, FmUserProcessHistoryRequest request) throws ServiceException {
        tenantAccessGuard.requireAccess(tenantId);
        String relation = normalized(request == null ? null : request.relation(), "ALL");
        String status = normalized(request == null ? null : request.status(), null);
        int page = request == null || request.page() == null ? 1 : request.page();
        int pageSize = request == null || request.pageSize() == null ? 20 : request.pageSize();
        if (!RELATIONS.contains(relation) || (status != null && !STATUSES.contains(status))
                || page < 1 || !PAGE_SIZES.contains(pageSize)) {
            throw new ServiceException("查詢條件不正確");
        }
        Date startDate = date(request == null ? null : request.startDate(), false);
        Date endDate = date(request == null ? null : request.endDate(), true);
        Date actionStartDate = date(request == null ? null : request.actionStartDate(), false);
        Date actionEndDate = date(request == null ? null : request.actionEndDate(), true);
        validateRange(startDate, endDate);
        validateRange(actionStartDate, actionEndDate);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("account", UserUtils.getCurrentUser().getUsername());
        parameters.put("relation", relation);
        parameters.put("status", status);
        parameters.put("outcome", normalized(request == null ? null : request.outcome(), null));
        String keyword = normalized(request == null ? null : request.keyword(), null);
        parameters.put("keywordLike", keyword == null ? null : escapedLike(keyword));
        parameters.put("startDate", startDate);
        parameters.put("endDate", endDate);
        parameters.put("actionStartDate", actionStartDate);
        parameters.put("actionEndDate", actionEndDate);
        parameters.put("limit", pageSize);
        parameters.put("offset", (page - 1) * pageSize);

        long totalCount = processInstanceService.countUserHistory(parameters);
        List<FmUserProcessHistoryView> items = processInstanceService.userHistory(parameters)
                .stream().map(this::view).toList();
        int totalPages = Math.max(1, (int) Math.ceil(totalCount / (double) pageSize));
        return success(new FmUserProcessHistoryPageView(
                items, totalCount, totalPages, page, pageSize));
    }

    @Override
    public DefaultResult<FmRequestTrackDetailView> load(
            String tenantId, String processInstanceId) throws ServiceException {
        tenantAccessGuard.requireAccess(tenantId);
        return trackingLogicService.load(tenantId, processInstanceId);
    }

    @Override
    public DefaultResult<FmRequestProcessDiagramView> diagram(
            String tenantId, String processInstanceId) throws ServiceException {
        tenantAccessGuard.requireAccess(tenantId);
        return trackingLogicService.diagram(tenantId, processInstanceId);
    }

    private FmUserProcessHistoryView view(FmUserProcessHistoryRow row) {
        List<String> relations = new ArrayList<>();
        if (Boolean.TRUE.equals(row.getApplicant())) relations.add("APPLICANT");
        if (Boolean.TRUE.equals(row.getStarter())) relations.add("STARTER");
        if (Boolean.TRUE.equals(row.getApprover())) relations.add("APPROVER");
        List<String> currentTasks = "RUNNING".equals(row.getInstanceStatus())
                ? taskService.createTaskQuery().processInstanceId(row.getProcessInstanceId())
                        .list().stream().map(value -> value.getName())
                        .filter(StringUtils::isNotBlank).distinct().toList()
                : List.of();
        return new FmUserProcessHistoryView(
                row.getProcessInstanceId(), row.getBusinessKey(), row.getDocumentNumber(),
                row.getProcessName(), row.getFormName(), row.getApplicantAccount(),
                row.getStarterAccount(), List.copyOf(relations), row.getLastActionType(),
                row.getLastOutcome(), row.getLastActionDate(), row.getInstanceStatus(),
                currentTasks, row.getStartDate(), row.getEndDate());
    }

    private Date date(String value, boolean nextDay) throws ServiceException {
        if (StringUtils.isBlank(value)) return null;
        try {
            LocalDate day = LocalDate.parse(value.trim());
            if (nextDay) day = day.plusDays(1);
            return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException exception) {
            throw new ServiceException("日期格式必須為 yyyy-MM-dd");
        }
    }

    private void validateRange(Date start, Date end) throws ServiceException {
        if (start != null && end != null && !start.before(end)) {
            throw new ServiceException("開始日期不得晚於結束日期");
        }
    }

    private String normalized(String value, String defaultValue) {
        return StringUtils.defaultIfBlank(StringUtils.trimToNull(value), defaultValue);
    }

    private String escapedLike(String keyword) {
        return "%" + keyword.replace("\\", "\\\\")
                .replace("%", "\\%").replace("_", "\\_") + "%";
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
