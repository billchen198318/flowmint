package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmTaskActionRequest;
import org.qifu.fm.dto.command.FmTaskAddSignRequest;
import org.qifu.fm.dto.command.FmTaskTransferRequest;
import org.qifu.fm.dto.command.FmTaskDelegationRequest;
import org.qifu.fm.dto.command.FmTaskResolveRequest;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.dto.view.FmTaskDetailView;
import org.qifu.fm.dto.view.FmTaskInboxView;
import org.qifu.fm.dto.view.FmOptionView;

public interface IFmTaskRuntimeLogicService {

    DefaultResult<List<FmTaskInboxView>> inbox(String tenantId)
            throws ServiceException;

    DefaultResult<FmTaskDetailView> load(String tenantId, String taskId)
            throws ServiceException;

    DefaultResult<FmTaskActionResultView> action(
            String tenantId, FmTaskActionRequest request) throws ServiceException;

    DefaultResult<List<FmOptionView>> transferOptions(
            String tenantId, String taskId) throws ServiceException;

    DefaultResult<FmTaskActionResultView> transfer(
            String tenantId, FmTaskTransferRequest request) throws ServiceException;

    DefaultResult<FmTaskActionResultView> delegate(
            String tenantId, FmTaskDelegationRequest request) throws ServiceException;

    DefaultResult<FmTaskActionResultView> resolve(
            String tenantId, FmTaskResolveRequest request) throws ServiceException;

    DefaultResult<List<FmOptionView>> addSignOptions(
            String tenantId, String taskId) throws ServiceException;

    DefaultResult<FmTaskActionResultView> addSign(
            String tenantId, FmTaskAddSignRequest request) throws ServiceException;

    DefaultResult<FmTaskActionResultView> completeAddSign(
            String tenantId, FmTaskResolveRequest request) throws ServiceException;
}
