package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.view.FmAiProviderOptionView;
import org.qifu.fm.dto.command.FmAiAnalysisCommand;
import org.qifu.fm.dto.view.FmAiAnalysisView;

public interface IFmAiTaskRuntimeLogicService {

	DefaultResult<List<FmAiProviderOptionView>> providerOptions(
			String tenantId, String taskId) throws ServiceException;

	DefaultResult<FmAiAnalysisView> analyze(
			String tenantId, FmAiAnalysisCommand command) throws ServiceException;
}
