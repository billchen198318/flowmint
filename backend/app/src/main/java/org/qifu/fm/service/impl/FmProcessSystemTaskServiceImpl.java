package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessSystemTask;
import org.qifu.fm.mapper.FmProcessSystemTaskMapper;
import org.qifu.fm.service.IFmProcessSystemTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmProcessSystemTaskServiceImpl extends BaseService<FmProcessSystemTask, String>
        implements IFmProcessSystemTaskService {

    private final FmProcessSystemTaskMapper mapper;

    public FmProcessSystemTaskServiceImpl(FmProcessSystemTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessSystemTask, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public List<FmProcessSystemTask> findVersion(String tenantId, String processDefId, Integer versionNo)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processDefId", processDefId);
        parameters.put("versionNo", versionNo);
        return selectListByParams(parameters, "NODE_ID", "ASC").getValue();
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void replaceDraftVersion(String tenantId, String processDefId, Integer versionNo,
            List<FmProcessSystemTask> bindings) throws ServiceException {
        // The owning Logic must lock and verify the parent draft before replacing its children.
        for (FmProcessSystemTask binding : bindings) {
            if (!tenantId.equals(binding.getTenantId()) || !processDefId.equals(binding.getProcessDefId())
                    || !versionNo.equals(binding.getVersionNo())) {
                throw new ServiceException("Groovy binding 與流程版本不一致");
            }
        }
        mapper.deleteDraftVersion(Map.of("tenantId", tenantId, "processDefId", processDefId,
                "versionNo", versionNo));
        for (FmProcessSystemTask binding : bindings) {
            insert(binding);
        }
    }
}
