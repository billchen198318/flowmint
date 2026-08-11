package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmProcessMonitorRequest;
import org.qifu.fm.dto.view.FmProcessMonitorDetailView;
import org.qifu.fm.dto.view.FmProcessMonitorPageView;
import org.qifu.fm.dto.view.FmProcessMonitorView;

public interface IFmProcessMonitorLogicService {

	DefaultResult<FmProcessMonitorPageView> find(
			String tenantId, FmProcessMonitorRequest request) throws ServiceException;

	DefaultResult<FmProcessMonitorDetailView> load(
			String tenantId, String processInstanceId) throws ServiceException;
}
