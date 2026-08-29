package org.qifu.fm.service;

import java.util.Date;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmApiClientKey;

public interface IFmApiClientKeyService extends IBaseService<FmApiClientKey, String> {
	FmApiClientKey selectByKeyId(String keyId) throws ServiceException;
	void revoke(FmApiClientKey key, String reason) throws ServiceException;
	void markUsed(String keyId, Date usedAt, String sourceIp) throws ServiceException;
}
