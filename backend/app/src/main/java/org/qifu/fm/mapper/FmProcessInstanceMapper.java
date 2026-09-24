package org.qifu.fm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.model.FmOperationsProcessSummary;
import org.qifu.fm.model.FmOperationsDailySummary;
import org.qifu.fm.model.FmOperationsProcessRanking;
import org.qifu.fm.model.FmOperationsTaskRanking;
import org.qifu.fm.model.FmUserProcessHistoryRow;
import java.util.List;

@Mapper
public interface FmProcessInstanceMapper extends IBaseMapper<FmProcessInstance, String> {

	public FmProcessInstance lockInstance(Map<String, Object> paramMap);

    int updateStatus(Map<String, Object> parameters);

    FmOperationsProcessSummary selectOperationsSummary(Map<String, Object> parameters);

    List<FmOperationsDailySummary> selectOperationsDailySummary(
            Map<String, Object> parameters);

    List<FmOperationsProcessRanking> selectOperationsProcessRanking(
            Map<String, Object> parameters);

    List<FmOperationsTaskRanking> selectOperationsTaskRanking(
            Map<String, Object> parameters);

    List<FmUserProcessHistoryRow> selectUserHistory(Map<String, Object> parameters);

    long countUserHistory(Map<String, Object> parameters);
}
