package org.qifu.fm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.model.FmOperationsProcessSummary;

@Mapper
public interface FmProcessInstanceMapper extends IBaseMapper<FmProcessInstance, String> {

    int updateStatus(Map<String, Object> parameters);

    FmOperationsProcessSummary selectOperationsSummary(Map<String, Object> parameters);
}
