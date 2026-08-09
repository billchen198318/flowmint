package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.view.FmProcessSubmitView;

public interface IFmProcessRuntimeLogicService {

    DefaultResult<FmProcessSubmitView> submit(FmProcessSubmitCommand command)
            throws ServiceException;
}
