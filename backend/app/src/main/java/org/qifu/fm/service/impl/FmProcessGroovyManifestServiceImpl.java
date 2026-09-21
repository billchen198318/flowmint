package org.qifu.fm.service.impl;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessGroovyManifest;
import org.qifu.fm.mapper.FmProcessGroovyManifestMapper;
import org.qifu.fm.service.IFmProcessGroovyManifestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmProcessGroovyManifestServiceImpl extends BaseService<FmProcessGroovyManifest, String>
        implements IFmProcessGroovyManifestService {

    private final FmProcessGroovyManifestMapper mapper;

    public FmProcessGroovyManifestServiceImpl(FmProcessGroovyManifestMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessGroovyManifest, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public DefaultResult<FmProcessGroovyManifest> update(FmProcessGroovyManifest value) throws ServiceException {
        throw new ServiceException("SYSTEM_TASK_LEDGER_UPDATE_DENIED");
    }

    @Override
    public DefaultResult<Boolean> delete(FmProcessGroovyManifest value) throws ServiceException {
        throw new ServiceException("SYSTEM_TASK_LEDGER_DELETE_DENIED");
    }
}
