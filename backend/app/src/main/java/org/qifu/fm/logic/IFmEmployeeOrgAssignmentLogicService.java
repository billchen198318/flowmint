package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmEmployeeOrgAssignmentCommand;
import org.qifu.fm.dto.view.FmEmployeeOrgAssignmentView;
import org.qifu.fm.dto.view.FmOptionView;

public interface IFmEmployeeOrgAssignmentLogicService {

	DefaultResult<List<FmEmployeeOrgAssignmentView>> list(String employeeOid) throws ServiceException;

	DefaultResult<List<FmEmployeeOrgAssignmentView>> save(FmEmployeeOrgAssignmentCommand command)
			throws ServiceException;

	DefaultResult<List<FmEmployeeOrgAssignmentView>> deactivate(String employeeOid, String oid)
			throws ServiceException;

	DefaultResult<List<FmOptionView>> orgUnitOptions(String employeeOid) throws ServiceException;

	DefaultResult<List<FmOptionView>> titleOptions(String employeeOid) throws ServiceException;

	DefaultResult<List<FmOptionView>> managerOptions(String employeeOid) throws ServiceException;
}
