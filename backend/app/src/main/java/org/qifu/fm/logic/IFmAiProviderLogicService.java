package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmAiProviderCommand;
import org.qifu.fm.dto.view.FmAiProviderView;
import org.qifu.fm.entity.FmAiProvider;

public interface IFmAiProviderLogicService {

	DefaultResult<FmAiProviderView> create(FmAiProviderCommand command)
			throws ServiceException;
	DefaultResult<FmAiProviderView> load(String oid) throws ServiceException;
	DefaultResult<FmAiProviderView> update(FmAiProviderCommand command)
			throws ServiceException;
	DefaultResult<FmAiProviderView> deactivate(String oid) throws ServiceException;
	FmAiProviderView view(FmAiProvider provider) throws ServiceException;
}
