package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.mapper.FmOrgTitleMapper;
import org.qifu.fm.service.IFmOrgTitleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmOrgTitleServiceImpl extends BaseService<FmOrgTitle, String>
    implements IFmOrgTitleService {
  private final FmOrgTitleMapper mapper;
  public FmOrgTitleServiceImpl(FmOrgTitleMapper value) { mapper = value; }
  protected IBaseMapper<FmOrgTitle, String> getBaseMapper() { return mapper; }
}
