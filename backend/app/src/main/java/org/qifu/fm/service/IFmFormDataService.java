package org.qifu.fm.service;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmFormData;

public interface IFmFormDataService extends IBaseService<FmFormData, String> {

	String lockByFormDataId(String tenantId, String formDataId);

	int updateDataContent(String tenantId, String formDataId,
			String dataContent, int expectedLockVersion);

}
