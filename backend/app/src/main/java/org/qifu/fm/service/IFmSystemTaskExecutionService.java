package org.qifu.fm.service;

import java.util.Date;
import java.util.List;
import org.qifu.base.exception.ServiceException;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmSystemTaskExecution;

public interface IFmSystemTaskExecutionService extends IBaseService<FmSystemTaskExecution, String> {

    int claim(String tenantId, String oid, int generation, Date now, Date leaseUntil) throws ServiceException;

    int rearm(String tenantId, String oid, int generation) throws ServiceException;

    List<FmSystemTaskExecution> findAutomaticRetries(String tenantId, Date now, String afterOid, int limit)
            throws ServiceException;

    List<FmSystemTaskExecution> findStalledReady(String tenantId, Date cutoff, String afterOid, int limit) throws ServiceException;

    int finishReady(String tenantId, String oid, int generation, Date cutoff, Date now, String status, String errorCode)
            throws ServiceException;

    int complete(String tenantId, String oid, int generation, Date now,
            String resultSha256, String formSnapshotOid) throws ServiceException;

    int cancel(String tenantId, String oid, Date now) throws ServiceException;

    FmSystemTaskExecution lockInvocation(String tenantId, String invocationId) throws ServiceException;

    int fail(String tenantId, String oid, int generation, Date now, String errorCode) throws ServiceException;

    List<FmSystemTaskExecution> findExpired(String tenantId, Date now, int limit) throws ServiceException;

    int expire(String tenantId, String oid, int generation, Date now) throws ServiceException;

    List<FmSystemTaskExecution> lockActiveForProcess(String tenantId, String processInstanceId);

    List<FmSystemTaskExecution> findIncidents(String tenantId, String invocationId, int offset, int limit)
            throws ServiceException;
}
