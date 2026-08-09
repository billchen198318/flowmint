package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmApprovalAuthorityCommand;
import org.qifu.fm.dto.view.FmApprovalAuthorityView;

public interface IFmApprovalAuthorityLogicService {

    DefaultResult<List<FmApprovalAuthorityView>> findByProcess(
            String tenantId,
            String processDefId) throws ServiceException;

    DefaultResult<FmApprovalAuthorityView> load(String oid) throws ServiceException;

    DefaultResult<FmApprovalAuthorityView> create(FmApprovalAuthorityCommand command)
            throws ServiceException;

    DefaultResult<FmApprovalAuthorityView> update(FmApprovalAuthorityCommand command)
            throws ServiceException;

    DefaultResult<FmApprovalAuthorityView> deactivate(String oid) throws ServiceException;
}
