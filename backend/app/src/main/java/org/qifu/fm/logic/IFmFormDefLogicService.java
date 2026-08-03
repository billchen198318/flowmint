package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmFormDefCommand;
import org.qifu.fm.dto.command.FmFormVersionCommand;
import org.qifu.fm.dto.view.FmFormDefView;
import org.qifu.fm.dto.view.FmOptionView;

public interface IFmFormDefLogicService {

    DefaultResult<FmFormDefView> create(FmFormDefCommand command) throws ServiceException;

    DefaultResult<FmFormDefView> load(String oid, String message) throws ServiceException;

    DefaultResult<FmFormDefView> update(FmFormDefCommand command) throws ServiceException;

    DefaultResult<FmFormDefView> deactivate(String oid) throws ServiceException;

    DefaultResult<FmFormDefView> saveDraft(FmFormVersionCommand command)
            throws ServiceException;

    DefaultResult<FmFormDefView> createVersion(String formDefOid)
            throws ServiceException;

    DefaultResult<FmFormDefView> publish(String versionOid) throws ServiceException;

    DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;
}
