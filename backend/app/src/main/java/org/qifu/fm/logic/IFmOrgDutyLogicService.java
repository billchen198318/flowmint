package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmEmployeeDutyCommand;
import org.qifu.fm.dto.command.FmOrgDutyCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgDutyView;
import org.qifu.fm.entity.FmOrgDuty;

public interface IFmOrgDutyLogicService {

	DefaultResult<FmOrgDutyView> create(FmOrgDutyCommand command) throws ServiceException;

	DefaultResult<FmOrgDutyView> load(String oid, String message) throws ServiceException;

	DefaultResult<FmOrgDutyView> update(FmOrgDutyCommand command) throws ServiceException;

	DefaultResult<FmOrgDutyView> deactivate(String oid) throws ServiceException;

	DefaultResult<FmOrgDutyView> saveAssignee(FmEmployeeDutyCommand command) throws ServiceException;

	DefaultResult<FmOrgDutyView> deactivateAssignee(String dutyOid, String oid) throws ServiceException;

	FmOrgDutyView view(FmOrgDuty duty) throws ServiceException;

	DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;

	DefaultResult<List<FmOptionView>> orgUnitOptions(String tenantId) throws ServiceException;

	DefaultResult<List<FmOptionView>> assignmentOptions(String dutyOid) throws ServiceException;
}
