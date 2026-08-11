package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.mapper.FmProcessInstanceMapper;
import org.qifu.fm.model.FmOperationsProcessSummary;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmProcessInstanceServiceImpl extends BaseService<FmProcessInstance, String>
        implements IFmProcessInstanceService {

    private final FmProcessInstanceMapper mapper;

    public FmProcessInstanceServiceImpl(FmProcessInstanceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessInstance, String> getBaseMapper() {
        return mapper;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean updateStatus(
            String tenantId,
            String processInstanceId,
            String currentStatus,
            String targetStatus,
            Date endDate,
            String updateAccount) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("currentStatus", currentStatus);
        parameters.put("targetStatus", targetStatus);
        parameters.put("endDate", endDate);
        parameters.put("uuserid", updateAccount);
        parameters.put("udate", new Date());
        return mapper.updateStatus(parameters) == 1;
    }

    @Override
    public FmOperationsProcessSummary operationsSummary(
            String tenantId, Date startDate, Date endDate) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("startDate", startDate);
        parameters.put("endDate", endDate);
        return mapper.selectOperationsSummary(parameters);
    }
}
