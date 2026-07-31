package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmTenantAccountCommand;
import org.qifu.fm.dto.command.FmTenantCommand;
import org.qifu.fm.dto.command.FmResetPasswordCommand;
import org.qifu.fm.dto.view.FmTenantView;

public interface IFmTenantLogicService {
	DefaultResult<FmTenantView> create(FmTenantCommand command) throws ServiceException;

	DefaultResult<FmTenantView> load(String oid, String message) throws ServiceException;

	DefaultResult<FmTenantView> update(FmTenantCommand command) throws ServiceException;

	DefaultResult<FmTenantView> deactivate(String oid) throws ServiceException;

	DefaultResult<FmTenantView> addAccount(FmTenantAccountCommand command) throws ServiceException;

	DefaultResult<FmTenantView> updateAccount(FmTenantAccountCommand command) throws ServiceException;

	DefaultResult<FmTenantView> resetPassword(FmResetPasswordCommand command) throws ServiceException;

	DefaultResult<FmTenantView> deactivateAccount(String tenantOid, String account) throws ServiceException;

	DefaultResult<FmTenantView> activateAccount(String tenantOid, String account) throws ServiceException;
}