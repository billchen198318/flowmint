package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyRuntimePreparation;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest;

/** Internal runtime entry; identities must come from the persisted process instance. */
public interface IFmGroovyRuntimeDefinitionLogicService {

    FmGroovyRuntimePreparation load(String tenantId, String processDefId, int versionNo,
            String nodeId, String formId, int formVersionNo,
            FmGroovyVersionManifest.EngineProfile trustedProfile) throws ServiceException;
}
