package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmOrgLevelScheme;
import org.qifu.fm.mapper.FmOrgLevelSchemeMapper;
import org.qifu.fm.service.IFmOrgLevelSchemeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmOrgLevelSchemeServiceImpl extends BaseService<FmOrgLevelScheme, String>
		implements IFmOrgLevelSchemeService {

	private final FmOrgLevelSchemeMapper mapper;

	public FmOrgLevelSchemeServiceImpl(FmOrgLevelSchemeMapper v) {
		mapper = v;
	}

	protected IBaseMapper<FmOrgLevelScheme, String> getBaseMapper() {
		return mapper;
	}
}
