package org.qifu.fm.domain.ai;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmAiAnalysisAccess;
import org.qifu.fm.service.IFmAiAnalysisAccessService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FmAiAccessLedger {

	private static final Set<String> ACCESS_TYPES = Set.of(
			"CLICK", "START", "CACHE_HIT", "READ", "SUCCEEDED", "FAILED");
	private final IFmAiAnalysisAccessService accessService;

	public FmAiAccessLedger(IFmAiAnalysisAccessService accessService) {
		this.accessService = accessService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class)
	public void record(String tenantId, String analysisId, String taskId,
			String actorAccount, String accessType, String resultStatus,
			String errorCode) throws ServiceException {
		if (StringUtils.isAnyBlank(tenantId, taskId, actorAccount)
				|| !ACCESS_TYPES.contains(accessType)
				|| !Set.of("SUCCESS", "FAILURE").contains(resultStatus)) {
			throw new ServiceException("AI Analysis Access 稽核參數錯誤");
		}
		FmAiAnalysisAccess access = new FmAiAnalysisAccess();
		access.setTenantId(tenantId);
		access.setAccessId(UUID.randomUUID().toString());
		access.setAnalysisId(StringUtils.trimToNull(analysisId));
		access.setTaskId(taskId);
		access.setActorAccount(actorAccount);
		access.setAccessType(accessType);
		access.setResultStatus(resultStatus);
		access.setErrorCode(StringUtils.abbreviate(
				StringUtils.trimToNull(errorCode), 50));
		access.setRequestDate(new Date());
		accessService.insert(access);
	}
}
