package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmOrgTitleCommand;
import org.qifu.fm.dto.view.FmOrgTitleView;

public interface IFmOrgTitleLogicService {
  DefaultResult<FmOrgTitleView> create(FmOrgTitleCommand command) throws ServiceException;
  DefaultResult<FmOrgTitleView> load(String oid, String message) throws ServiceException;
  DefaultResult<FmOrgTitleView> update(FmOrgTitleCommand command) throws ServiceException;
  DefaultResult<FmOrgTitleView> deactivate(String oid) throws ServiceException;
}
