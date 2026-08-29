package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmApiClientCommand;
import org.qifu.fm.dto.command.FmApiKeyIssueCommand;
import org.qifu.fm.dto.command.FmApiKeyRevokeCommand;
import org.qifu.fm.dto.view.FmApiClientKeyView;
import org.qifu.fm.dto.view.FmApiClientView;
import org.qifu.fm.dto.view.FmApiKeyIssueView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmApiClient;

public interface IFmExternalApiManagementLogicService {
	DefaultResult<FmApiClientView> create(FmApiClientCommand command)
			throws ServiceException;
	DefaultResult<FmApiClientView> load(String oid) throws ServiceException;
	DefaultResult<FmApiClientView> update(FmApiClientCommand command)
			throws ServiceException;
	DefaultResult<FmApiClientView> deactivate(String oid) throws ServiceException;
	DefaultResult<List<FmApiClientKeyView>> keys(String clientOid)
			throws ServiceException;
	DefaultResult<FmApiKeyIssueView> issueKey(FmApiKeyIssueCommand command)
			throws ServiceException;
	DefaultResult<FmApiKeyIssueView> rotateKey(FmApiKeyIssueCommand command)
			throws ServiceException;
	DefaultResult<FmApiClientKeyView> revokeKey(FmApiKeyRevokeCommand command)
			throws ServiceException;
	DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;
	FmApiClientView view(FmApiClient client) throws ServiceException;
}
