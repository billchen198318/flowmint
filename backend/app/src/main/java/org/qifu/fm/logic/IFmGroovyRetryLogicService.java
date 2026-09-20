package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmGroovyRetryCommand;
import org.qifu.fm.dto.view.FmGroovyRetryView;

public interface IFmGroovyRetryLogicService {
    DefaultResult<FmGroovyRetryView> preview(String tenantId, String invocationId) throws ServiceException;
    DefaultResult<Boolean> retry(String tenantId, FmGroovyRetryCommand command) throws ServiceException;
}
