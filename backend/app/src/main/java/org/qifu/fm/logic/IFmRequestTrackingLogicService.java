package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.view.FmRequestTrackDetailView;
import org.qifu.fm.dto.view.FmRequestProcessDiagramView;
import org.qifu.fm.dto.view.FmRequestTrackView;
import org.qifu.fm.dto.view.FmTaskActionResultView;

public interface IFmRequestTrackingLogicService {

    DefaultResult<List<FmRequestTrackView>> mine(String tenantId)
            throws ServiceException;

    DefaultResult<FmRequestTrackDetailView> load(
            String tenantId, String processInstanceId) throws ServiceException;

    DefaultResult<FmRequestProcessDiagramView> diagram(
            String tenantId, String processInstanceId) throws ServiceException;

    DefaultResult<FmTaskActionResultView> withdraw(
            String tenantId, String processInstanceId, String reason)
            throws ServiceException;

    DefaultResult<FmTaskActionResultView> cancel(
            String tenantId, String processInstanceId, String reason)
            throws ServiceException;
}
