package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmEmployeeCommand;
import org.qifu.fm.dto.view.FmEmployeeView;
import org.qifu.fm.dto.view.FmOptionView;

public interface IFmEmployeeLogicService {
	DefaultResult<FmEmployeeView> create(FmEmployeeCommand command) throws ServiceException;
	DefaultResult<FmEmployeeView> load(String oid, String message) throws ServiceException;
	DefaultResult<FmEmployeeView> update(FmEmployeeCommand command) throws ServiceException;
	DefaultResult<FmEmployeeView> deactivate(String oid) throws ServiceException;
	DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;
	DefaultResult<List<FmOptionView>> accountOptions(String tenantId) throws ServiceException;
}
