package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmOrgUnitHeadCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgUnitHeadView;

public interface IFmOrgUnitHeadLogicService {

	DefaultResult<FmOrgUnitHeadView> create(FmOrgUnitHeadCommand command) throws ServiceException;
	DefaultResult<FmOrgUnitHeadView> load(String oid, String message) throws ServiceException;
	DefaultResult<FmOrgUnitHeadView> update(FmOrgUnitHeadCommand command) throws ServiceException;
	DefaultResult<FmOrgUnitHeadView> deactivate(String oid) throws ServiceException;
	FmOrgUnitHeadView view(org.qifu.fm.entity.FmOrgUnitHead value) throws ServiceException;
	DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;
	DefaultResult<List<FmOptionView>> orgUnitOptions(String tenantId) throws ServiceException;
	DefaultResult<List<FmOptionView>> employeeOptions(String tenantId, String orgUnitId) throws ServiceException;
}
