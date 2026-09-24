package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmUserProcessHistoryRequest;
import org.qifu.fm.dto.view.FmRequestProcessDiagramView;
import org.qifu.fm.dto.view.FmRequestTrackDetailView;
import org.qifu.fm.dto.view.FmUserProcessHistoryPageView;

public interface IFmUserProcessHistoryLogicService {
    DefaultResult<FmUserProcessHistoryPageView> findPage(
            String tenantId, FmUserProcessHistoryRequest request) throws ServiceException;
    DefaultResult<FmRequestTrackDetailView> load(
            String tenantId, String processInstanceId) throws ServiceException;
    DefaultResult<FmRequestProcessDiagramView> diagram(
            String tenantId, String processInstanceId) throws ServiceException;
}
