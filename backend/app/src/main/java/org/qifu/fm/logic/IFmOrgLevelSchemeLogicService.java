package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmOrgLevelSchemeCommand;
import org.qifu.fm.dto.view.FmOrgLevelSchemeView;

public interface IFmOrgLevelSchemeLogicService {
  DefaultResult<FmOrgLevelSchemeView> create(FmOrgLevelSchemeCommand c)
    throws ServiceException;
  DefaultResult<FmOrgLevelSchemeView> load(String oid, String message)
    throws ServiceException;
  DefaultResult<FmOrgLevelSchemeView> update(FmOrgLevelSchemeCommand c)
    throws ServiceException;
  DefaultResult<FmOrgLevelSchemeView> deactivate(String oid)
    throws ServiceException;
}
