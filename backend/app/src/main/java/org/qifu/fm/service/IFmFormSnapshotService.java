package org.qifu.fm.service;

import org.qifu.base.service.IBaseService;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmFormSnapshot;

public interface IFmFormSnapshotService extends IBaseService<FmFormSnapshot, String> {

    String insertSystemTaskSnapshot(FmFormSnapshot snapshot) throws ServiceException;
}
