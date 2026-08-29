package org.qifu.fm.service;

import java.util.Date;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmApiAccessLog;

public interface IFmApiAccessLogService extends IBaseService<FmApiAccessLog, String> {
	long countClientRequestsSince(String tenantId, String clientId, Date since);
	void append(FmApiAccessLog accessLog);
}
