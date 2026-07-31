package org.qifu.fm.service;

import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.PageOf;
import org.qifu.base.model.QueryResult;
import org.qifu.base.service.IBaseService;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmOrgUnitVersion;

public interface IFmOrgUnitVersionService extends IBaseService<FmOrgUnitVersion, String> {

	DefaultResult<List<FmOrgUnitView>> selectCurrentTree(Map<String, Object> paramMap);

	QueryResult<List<FmOrgUnitView>> findCurrentPage(Map<String, Object> paramMap, PageOf pageOf) throws ServiceException;

	DefaultResult<Long> countCurrent(Map<String, Object> paramMap);
}
