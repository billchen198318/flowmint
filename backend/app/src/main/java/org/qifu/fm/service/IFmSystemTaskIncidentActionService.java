package org.qifu.fm.service;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;

public interface IFmSystemTaskIncidentActionService extends IBaseService<FmSystemTaskIncidentAction, String> {

    List<FmSystemTaskIncidentAction> history(String tenantId, String executionOid, int offset, int limit)
            throws ServiceException;

    List<FmSystemTaskIncidentAction> lockHistory(String tenantId, String executionOid, String requestId)
            throws ServiceException;
}
