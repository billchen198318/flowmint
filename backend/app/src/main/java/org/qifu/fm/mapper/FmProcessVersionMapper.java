package org.qifu.fm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmProcessVersion;

@Mapper
public interface FmProcessVersionMapper extends IBaseMapper<FmProcessVersion, String> {

    Integer findLockVersion(Map<String, Object> paramMap);

    Integer findDraftLockVersion(Map<String, Object> paramMap);

    Integer lockDraft(Map<String, Object> paramMap);

    int advanceDraftLock(Map<String, Object> paramMap);
}
