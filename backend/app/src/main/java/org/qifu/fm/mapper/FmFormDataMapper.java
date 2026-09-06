package org.qifu.fm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmFormData;

@Mapper
public interface FmFormDataMapper extends IBaseMapper<FmFormData, String> {

	public String lockByFormDataId(Map<String, Object> paramMap);

	public int updateDataContent(Map<String, Object> paramMap);

}
