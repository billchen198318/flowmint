package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmWorkflowDelegationCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmWorkflowDelegationView;
import org.qifu.fm.entity.FmWorkflowDelegation;

public interface IFmWorkflowDelegationLogicService {

	DefaultResult<FmWorkflowDelegationView> create(FmWorkflowDelegationCommand command)
			throws ServiceException;

	DefaultResult<FmWorkflowDelegationView> load(String oid, String message)
			throws ServiceException;

	DefaultResult<FmWorkflowDelegationView> update(FmWorkflowDelegationCommand command)
			throws ServiceException;

	DefaultResult<FmWorkflowDelegationView> deactivate(String oid) throws ServiceException;

	FmWorkflowDelegationView view(FmWorkflowDelegation delegation) throws ServiceException;

	DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;

	DefaultResult<List<FmOptionView>> accountOptions(String tenantId) throws ServiceException;

	DefaultResult<List<FmOptionView>> groupOptions(String tenantId) throws ServiceException;
}
