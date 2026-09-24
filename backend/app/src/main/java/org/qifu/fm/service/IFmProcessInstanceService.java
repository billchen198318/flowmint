package org.qifu.fm.service;

import java.util.Date;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.model.FmOperationsProcessSummary;
import org.qifu.fm.model.FmOperationsDailySummary;
import org.qifu.fm.model.FmOperationsProcessRanking;
import org.qifu.fm.model.FmOperationsTaskRanking;
import org.qifu.fm.model.FmUserProcessHistoryRow;
import java.util.Map;
import java.util.List;

public interface IFmProcessInstanceService extends IBaseService<FmProcessInstance, String> {

    FmProcessInstance lockInstance(String tenantId, String processInstanceId);

    boolean updateStatus(
            String tenantId,
            String processInstanceId,
            String currentStatus,
            String targetStatus,
            Date endDate,
            String updateAccount);

    FmOperationsProcessSummary operationsSummary(
            String tenantId, Date startDate, Date endDate);

    List<FmOperationsDailySummary> operationsDailySummary(
            String tenantId, Date startDate, Date endDate);

    List<FmOperationsProcessRanking> operationsProcessRanking(
            String tenantId, Date startDate, Date endDate);

    List<FmOperationsTaskRanking> operationsTaskRanking(
            String tenantId, Date startDate, Date endDate);

    List<FmUserProcessHistoryRow> userHistory(Map<String, Object> parameters);

    long countUserHistory(Map<String, Object> parameters);
}
