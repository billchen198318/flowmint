package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmGroovyPreviewCommand;
import org.qifu.fm.dto.view.FmGroovyPreviewView;

public interface IFmGroovyPreviewLogicService {

    DefaultResult<FmGroovyPreviewView> check(FmGroovyPreviewCommand command) throws ServiceException;

    DefaultResult<FmGroovyPreviewView> preview(FmGroovyPreviewCommand command) throws ServiceException;
}
