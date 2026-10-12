package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.ai.FmAiAccessLedger;
import org.qifu.fm.domain.ai.FmAiApiKeyCipher;
import org.qifu.fm.domain.ai.FmAiContentHashService;
import org.qifu.fm.domain.ai.FmAiContextBuilder;
import org.qifu.fm.domain.ai.FmAiProviderClientRegistry;
import org.qifu.fm.dto.command.FmAiAnalysisCommand;
import org.qifu.fm.dto.view.FmTaskDetailView;
import org.qifu.fm.logic.IFmTaskRuntimeLogicService;
import org.qifu.fm.service.IFmAiAnalysisService;
import org.qifu.fm.service.IFmAiProviderService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;

import tools.jackson.databind.ObjectMapper;

class FmAiTaskRuntimeAuditFailureTest {
	@Test
	void refusesAnalysisWhenClickAuditCannotBeWritten() throws Exception {
		IFmTaskRuntimeLogicService tasks = mock(IFmTaskRuntimeLogicService.class);
		DefaultResult<FmTaskDetailView> taskResult = new DefaultResult<>();
		taskResult.setValue(mock(FmTaskDetailView.class));
		when(tasks.load("TENANT", "TASK")).thenReturn(taskResult);
		IFmAiProviderService providers = mock(IFmAiProviderService.class);
		FmAiAccessLedger ledger = mock(FmAiAccessLedger.class);
		org.mockito.Mockito.doThrow(new ServiceException("database unavailable"))
				.when(ledger).record(eq("TENANT"), eq(null), eq("TASK"),
						eq("actor"), eq("CLICK"), eq("SUCCESS"), eq(null));
		var logic = new FmAiTaskRuntimeLogicServiceImpl(tasks, providers,
				mock(IFmProcessInstanceService.class), mock(IFmFormDataService.class),
				mock(IFmAiAnalysisService.class), mock(FmAiContextBuilder.class),
				mock(FmAiContentHashService.class), mock(FmAiApiKeyCipher.class),
				mock(FmAiProviderClientRegistry.class), ledger, new ObjectMapper());
		User user = mock(User.class);
		when(user.getUsername()).thenReturn("actor");
		try (var users = mockStatic(UserUtils.class)) {
			users.when(UserUtils::getCurrentUser).thenReturn(user);
			assertThrows(ServiceException.class, () -> logic.analyze("TENANT",
					new FmAiAnalysisCommand("TASK", "OPENAI", false)));
		}
		verifyNoInteractions(providers);
	}
}
