package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.command.FmProcessStartCatalogCommand;
import org.qifu.fm.dto.command.FmProcessStartLoadCommand;
import org.qifu.fm.dto.view.FmProcessStartCatalogView;
import org.qifu.fm.dto.view.FmProcessStartApplicantView;
import org.qifu.fm.dto.view.FmProcessStartLoadView;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.dto.view.FmRuntimeTenantView;

public interface IFmProcessRuntimeLogicService {

	DefaultResult<List<FmRuntimeTenantView>> tenants()
			throws ServiceException;

	DefaultResult<List<FmProcessStartApplicantView>> applicants(String tenantId)
			throws ServiceException;

	DefaultResult<List<FmProcessStartCatalogView>> catalog(
			FmProcessStartCatalogCommand command) throws ServiceException;

	DefaultResult<FmProcessStartLoadView> loadStart(FmProcessStartLoadCommand command)
			throws ServiceException;

	DefaultResult<FmProcessSubmitView> submit(FmProcessSubmitCommand command)
			throws ServiceException;
}
