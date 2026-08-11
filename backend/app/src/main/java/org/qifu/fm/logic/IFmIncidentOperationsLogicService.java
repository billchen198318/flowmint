package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmIncidentReassignRequest;
import org.qifu.fm.dto.command.FmIncidentRetryRequest;
import org.qifu.fm.dto.command.FmProcessTerminateRequest;
import org.qifu.fm.dto.view.FmAssignmentIncidentView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.dto.view.FmOptionView;

public interface IFmIncidentOperationsLogicService {
    DefaultResult<List<FmAssignmentIncidentView>> incidents(
            String tenantId, String status) throws ServiceException;

    DefaultResult<List<FmOptionView>> reassignOptions(
            String tenantId) throws ServiceException;

    DefaultResult<FmTaskActionResultView> reassign(
            String tenantId, FmIncidentReassignRequest request) throws ServiceException;

    DefaultResult<FmTaskActionResultView> retry(
            String tenantId, FmIncidentRetryRequest request) throws ServiceException;

    DefaultResult<FmTaskActionResultView> terminate(
            String tenantId, FmProcessTerminateRequest request) throws ServiceException;
}
