package org.qifu.fm.service;

import java.util.Date;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmAiProvider;

public interface IFmAiProviderService extends IBaseService<FmAiProvider, String> {
	void lockByTenant(String tenantId) throws ServiceException;
	void updateTestStatus(FmAiProvider provider, String status, Date testedAt)
			throws ServiceException;
}
