package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.view.FmGroovyIncidentPageView;
import org.qifu.fm.dto.view.FmGroovyIncidentView;

public interface IFmGroovyIncidentLogicService {

    DefaultResult<FmGroovyIncidentPageView> find(String tenantId, int offset) throws ServiceException;

    DefaultResult<FmGroovyIncidentView> detail(String tenantId, String invocationId) throws ServiceException;
}
