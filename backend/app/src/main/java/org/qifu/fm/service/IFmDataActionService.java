package org.qifu.fm.service;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmDataAction;

public interface IFmDataActionService extends IBaseService<FmDataAction, String> {

	boolean updateOptimistic(FmDataAction action, Integer expectedLockVersion)
			throws ServiceException;
}
