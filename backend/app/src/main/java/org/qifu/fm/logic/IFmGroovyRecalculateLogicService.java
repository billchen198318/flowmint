package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmGroovyRecalculateCommand;
import org.qifu.fm.dto.view.FmGroovyRecalculateView;

public interface IFmGroovyRecalculateLogicService {
    DefaultResult<FmGroovyRecalculateView> preview(String tenantId, String invocationId) throws ServiceException;
    DefaultResult<String> recalculate(String tenantId, FmGroovyRecalculateCommand command) throws ServiceException;
}
