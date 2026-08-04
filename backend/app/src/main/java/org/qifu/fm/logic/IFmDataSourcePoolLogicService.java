package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmDataSourcePoolCommand;
import org.qifu.fm.dto.view.FmDataSourcePoolView;
import org.qifu.fm.dto.view.FmDataSourceTestView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmDataSourcePool;

public interface IFmDataSourcePoolLogicService {

	DefaultResult<FmDataSourcePoolView> create(FmDataSourcePoolCommand command)
			throws ServiceException;
	DefaultResult<FmDataSourcePoolView> load(String oid, String message)
			throws ServiceException;
	DefaultResult<FmDataSourcePoolView> update(FmDataSourcePoolCommand command)
			throws ServiceException;
	DefaultResult<FmDataSourcePoolView> deactivate(String oid) throws ServiceException;
	DefaultResult<FmDataSourceTestView> test(FmDataSourcePoolCommand command)
			throws ServiceException;
	FmDataSourcePoolView view(FmDataSourcePool pool);
	DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;
}
