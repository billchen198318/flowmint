package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmEmployeeDuty;
import org.qifu.fm.mapper.FmEmployeeDutyMapper;
import org.qifu.fm.service.IFmEmployeeDutyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmEmployeeDutyServiceImpl extends BaseService<FmEmployeeDuty, String>
		implements IFmEmployeeDutyService {

	private final FmEmployeeDutyMapper mapper;

	public FmEmployeeDutyServiceImpl(FmEmployeeDutyMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmEmployeeDuty, String> getBaseMapper() {
		return mapper;
	}
}
