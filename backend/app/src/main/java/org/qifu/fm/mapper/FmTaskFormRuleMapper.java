package org.qifu.fm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.qifu.fm.dto.view.FmPublishedFormOptionView;
import org.qifu.fm.entity.FmTaskFormRule;

@Mapper
public interface FmTaskFormRuleMapper {

    List<FmTaskFormRule> selectByVersion(@Param("tenantId") String tenantId,
            @Param("processDefId") String processDefId,
            @Param("processVersionNo") Integer processVersionNo);

    int deleteByVersion(@Param("tenantId") String tenantId,
            @Param("processDefId") String processDefId,
            @Param("processVersionNo") Integer processVersionNo);

    int insert(FmTaskFormRule rule);

    List<FmPublishedFormOptionView> selectPublishedFormOptions(
            @Param("tenantId") String tenantId);

    int countPublishedFormVersion(
            @Param("tenantId") String tenantId,
            @Param("formId") String formId,
            @Param("formVersionNo") Integer formVersionNo);
}
