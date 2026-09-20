package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmGroovyIncidentHandleCommand;
import org.qifu.fm.dto.view.FmGroovyIncidentHandlingView;

public interface IFmGroovyIncidentHandlingLogicService {

    DefaultResult<FmGroovyIncidentHandlingView> load(String tenantId, String invocationId, int offset)
            throws ServiceException;

    DefaultResult<FmGroovyIncidentHandlingView> handle(String tenantId, FmGroovyIncidentHandleCommand command)
            throws ServiceException;
}
