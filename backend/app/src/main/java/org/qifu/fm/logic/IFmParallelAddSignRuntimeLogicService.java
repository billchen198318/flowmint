package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmParallelAddSignCancelRequest;
import org.qifu.fm.dto.command.FmParallelAddSignCompleteRequest;
import org.qifu.fm.dto.command.FmParallelAddSignStartRequest;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmParallelAddSignDetailView;
import org.qifu.fm.dto.view.FmTaskActionResultView;

public interface IFmParallelAddSignRuntimeLogicService {

    DefaultResult<List<FmOptionView>> options(
            String tenantId, String taskId) throws ServiceException;

    DefaultResult<FmParallelAddSignDetailView> start(
            String tenantId, FmParallelAddSignStartRequest request)
            throws ServiceException;

    DefaultResult<FmTaskActionResultView> complete(
            String tenantId, FmParallelAddSignCompleteRequest request)
            throws ServiceException;

    DefaultResult<FmTaskActionResultView> cancel(
            String tenantId, FmParallelAddSignCancelRequest request)
            throws ServiceException;

    DefaultResult<FmParallelAddSignDetailView> detail(
            String tenantId, String taskId) throws ServiceException;

    DefaultResult<List<FmParallelAddSignDetailView>> processDetails(
            String tenantId, String processInstanceId) throws ServiceException;
}
