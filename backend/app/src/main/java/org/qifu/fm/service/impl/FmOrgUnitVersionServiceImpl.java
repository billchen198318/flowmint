package org.qifu.fm.service.impl;

import java.util.List;
import java.util.Map;

import ognl.OgnlException;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.PageOf;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.base.service.BaseService;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmOrgUnitVersion;
import org.qifu.fm.mapper.FmOrgUnitVersionMapper;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmOrgUnitVersionServiceImpl extends BaseService<FmOrgUnitVersion, String>
		implements IFmOrgUnitVersionService {
	private final FmOrgUnitVersionMapper orgUnitVersionMapper;

	public FmOrgUnitVersionServiceImpl(FmOrgUnitVersionMapper orgUnitVersionMapper) {
		this.orgUnitVersionMapper = orgUnitVersionMapper;
	}

	@Override
	protected IBaseMapper<FmOrgUnitVersion, String> getBaseMapper() {
		return orgUnitVersionMapper;
	}

	@Override
	public DefaultResult<List<FmOrgUnitView>> selectCurrentTree(Map<String, Object> paramMap) {
		return success(orgUnitVersionMapper.selectCurrentTree(paramMap));
	}

	@Override
	public QueryResult<List<FmOrgUnitView>> findCurrentPage(Map<String, Object> paramMap, PageOf pageOf)
			throws ServiceException {
		try {
			return super.findPage("countCurrent", "findCurrentPage", paramMap, pageOf);
		} catch (OgnlException exception) {
			throw new ServiceException("部門分頁查詢失敗：" + exception.getMessage());
		}
	}

	@Override
	public DefaultResult<Long> countCurrent(Map<String, Object> paramMap) {
		return success(orgUnitVersionMapper.countCurrent(paramMap));
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
