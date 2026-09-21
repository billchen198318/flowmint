package org.qifu.fm.service;

import java.util.Date;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmSystemTaskAttempt;

public interface IFmSystemTaskAttemptService extends IBaseService<FmSystemTaskAttempt, String> {

    int cancelRunning(String tenantId, String executionOid, int generation, Date now) throws ServiceException;

    int finish(String tenantId, String executionOid, String requestId, int generation,
            String status, Date now, String errorCode) throws ServiceException;
}
