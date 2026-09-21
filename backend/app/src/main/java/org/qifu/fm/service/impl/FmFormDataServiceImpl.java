package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.mapper.FmFormDataMapper;
import org.qifu.fm.service.IFmFormDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmFormDataServiceImpl extends BaseService<FmFormData, String>
        implements IFmFormDataService {

    private final FmFormDataMapper mapper;

    public FmFormDataServiceImpl(FmFormDataMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmFormData, String> getBaseMapper() {
        return mapper;
    }

	@Override
	@Transactional(readOnly = false)
	public String lockByFormDataId(String tenantId, String formDataId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("tenantId", tenantId);
		paramMap.put("formDataId", formDataId);
		return mapper.lockByFormDataId(paramMap);
	}

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    public FmFormData lockStateByFormDataId(String tenantId, String formDataId) {
        if (tenantId == null || tenantId.isBlank() || formDataId == null || formDataId.isBlank()) {
            throw new IllegalArgumentException("FORM_DATA_IDENTITY_REQUIRED");
        }
        return mapper.lockStateByFormDataId(Map.of("tenantId", tenantId, "formDataId", formDataId));
    }

	@Override
	@Transactional(readOnly = false)
	public int updateDataContent(String tenantId, String formDataId,
			String dataContent, int expectedLockVersion) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("tenantId", tenantId);
		paramMap.put("formDataId", formDataId);
		paramMap.put("dataContent", dataContent);
		paramMap.put("expectedLockVersion", expectedLockVersion);
		return mapper.updateDataContent(paramMap);
	}
}
