package org.qifu.fm.service;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmProcessSystemTask;

public interface IFmProcessSystemTaskService extends IBaseService<FmProcessSystemTask, String> {

    List<FmProcessSystemTask> findVersion(String tenantId, String processDefId, Integer versionNo)
            throws ServiceException;

    void replaceDraftVersion(String tenantId, String processDefId, Integer versionNo,
            List<FmProcessSystemTask> bindings) throws ServiceException;
}
