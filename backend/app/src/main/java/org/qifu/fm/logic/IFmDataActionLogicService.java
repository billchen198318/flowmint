package org.qifu.fm.logic;

import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmDataActionCommand;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.dto.view.FmDataActionView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmDataAction;

public interface IFmDataActionLogicService {

	DefaultResult<FmDataActionView> create(FmDataActionCommand command)
			throws ServiceException;

	DefaultResult<FmDataActionView> update(FmDataActionCommand command)
			throws ServiceException;

	DefaultResult<FmDataActionView> load(String oid, String message)
			throws ServiceException;

	DefaultResult<FmDataActionView> publish(String oid)
			throws ServiceException;

	DefaultResult<FmDataActionExecutionView> preview(String tenantId, String actionId,
			Integer versionNo, Map<String, Object> request, String loginAccount)
			throws ServiceException;

	DefaultResult<FmDataActionExecutionView> execute(String tenantId,
			String actionCode, Integer versionNo, Map<String, Object> request,
			String loginAccount) throws ServiceException;

	DefaultResult<List<FmOptionView>> poolOptions(String tenantId)
			throws ServiceException;

	DefaultResult<List<FmOptionView>> publishedOptions(String tenantId)
			throws ServiceException;

	DefaultResult<Map<String, Object>> metadata(String tenantId,
			String actionCode) throws ServiceException;

	FmDataActionView view(FmDataAction action) throws ServiceException;
}
