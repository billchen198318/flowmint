package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.mapper.FmEmployeeMapper;
import org.qifu.fm.service.IFmEmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmEmployeeServiceImpl extends BaseService<FmEmployee, String> implements IFmEmployeeService {
	private final FmEmployeeMapper employeeMapper;

	public FmEmployeeServiceImpl(FmEmployeeMapper employeeMapper) {
		this.employeeMapper = employeeMapper;
	}

	@Override
	protected IBaseMapper<FmEmployee, String> getBaseMapper() {
		return employeeMapper;
	}
}
