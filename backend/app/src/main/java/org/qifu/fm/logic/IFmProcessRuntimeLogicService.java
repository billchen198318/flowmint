package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.command.FmProcessStartLoadCommand;
import org.qifu.fm.dto.view.FmProcessStartLoadView;
import org.qifu.fm.dto.view.FmProcessSubmitView;

public interface IFmProcessRuntimeLogicService {

	DefaultResult<FmProcessStartLoadView> loadStart(FmProcessStartLoadCommand command)
			throws ServiceException;

    DefaultResult<FmProcessSubmitView> submit(FmProcessSubmitCommand command)
            throws ServiceException;
}
