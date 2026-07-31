package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmEmployee;

@Mapper
public interface FmEmployeeMapper extends IBaseMapper<FmEmployee, String> {
}
