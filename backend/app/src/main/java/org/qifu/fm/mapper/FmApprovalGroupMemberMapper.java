package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmApprovalGroupMember;

@Mapper
public interface FmApprovalGroupMemberMapper extends IBaseMapper<FmApprovalGroupMember, String> {
}
