package org.qifu.fm.logic;

import java.time.Instant;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeMapping.InputSnapshot;
import org.qifu.fm.domain.workflow.FmGroovyRuntimePreparation;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest.EngineProfile;

/** Internal entry for a scheduler; no request/form/Flowable variables supply business identity. */
public interface IFmGroovyRuntimeInputLogicService {

    LoadedInput load(String tenantId, String executionId, String invocationId, Instant startedAt,
            EngineProfile profile) throws ServiceException;

    record LoadedInput(String tenantId, String executionId, String processInstanceId, String processDefId,
            int processVersionNo, String nodeId, String formDataId, int expectedFormLock,
            String savedFormJson, InputSnapshot snapshot, FmGroovyRuntimePreparation preparation) {
    }
}
