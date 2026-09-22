package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.view.FmOrganizationHealthView;

public interface IFmOrganizationHealthLogicService {

	DefaultResult<FmOrganizationHealthView> inspect(String tenantId) throws ServiceException;

}
