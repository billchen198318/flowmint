package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmApprovalGroupCommand;
import org.qifu.fm.dto.command.FmApprovalGroupMemberCommand;
import org.qifu.fm.dto.view.FmApprovalGroupView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmApprovalGroup;

public interface IFmApprovalGroupLogicService {

	DefaultResult<FmApprovalGroupView> create(FmApprovalGroupCommand command) throws ServiceException;

	DefaultResult<FmApprovalGroupView> load(String oid, String message) throws ServiceException;

	DefaultResult<FmApprovalGroupView> update(FmApprovalGroupCommand command) throws ServiceException;

	DefaultResult<FmApprovalGroupView> deactivate(String oid) throws ServiceException;

	DefaultResult<FmApprovalGroupView> saveMember(FmApprovalGroupMemberCommand command)
			throws ServiceException;

	DefaultResult<FmApprovalGroupView> deactivateMember(String groupOid, String oid)
			throws ServiceException;

	FmApprovalGroupView view(FmApprovalGroup group) throws ServiceException;

	DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;

	DefaultResult<List<FmOptionView>> employeeOptions(String groupOid) throws ServiceException;
}
