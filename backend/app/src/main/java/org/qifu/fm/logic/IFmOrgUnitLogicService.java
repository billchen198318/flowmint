package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmOrgUnitCommand;
import org.qifu.fm.dto.command.FmOrgUnitMoveCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgUnitMovePreviewView;
import org.qifu.fm.dto.view.FmOrgUnitView;

public interface IFmOrgUnitLogicService {

	DefaultResult<FmOrgUnitView> create(FmOrgUnitCommand command) throws ServiceException;

	DefaultResult<FmOrgUnitView> load(String oid, String message) throws ServiceException;

	DefaultResult<FmOrgUnitView> update(FmOrgUnitCommand command) throws ServiceException;

	DefaultResult<FmOrgUnitView> deactivate(String oid, Integer currentVersionNo) throws ServiceException;

	DefaultResult<List<FmOrgUnitView>> tree(String tenantId, boolean includeInactive) throws ServiceException;

	DefaultResult<FmOrgUnitMovePreviewView> previewMove(FmOrgUnitMoveCommand command) throws ServiceException;

	DefaultResult<List<FmOrgUnitView>> move(FmOrgUnitMoveCommand command) throws ServiceException;

	DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException;
}
