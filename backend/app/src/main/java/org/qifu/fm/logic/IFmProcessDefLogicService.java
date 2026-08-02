package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmProcessDefCommand;
import org.qifu.fm.dto.command.FmProcessVersionCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmProcessDefView;
import org.qifu.fm.entity.FmProcessDef;

public interface IFmProcessDefLogicService {

    DefaultResult<FmProcessDefView> create(FmProcessDefCommand command) throws ServiceException;

    DefaultResult<FmProcessDefView> load(String oid, String message) throws ServiceException;

    DefaultResult<FmProcessDefView> update(FmProcessDefCommand command) throws ServiceException;

    DefaultResult<FmProcessDefView> deactivate(String oid) throws ServiceException;

    DefaultResult<FmProcessDefView> saveDraft(FmProcessVersionCommand command)
            throws ServiceException;

    DefaultResult<FmProcessDefView> createVersion(String processDefOid) throws ServiceException;

    DefaultResult<FmProcessDefView> publish(String versionOid) throws ServiceException;

    DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;

    FmProcessDefView view(FmProcessDef processDef) throws ServiceException;
}