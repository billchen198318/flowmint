package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.entity.FmDataActionStep;

class FmDataActionStreamingPolicyTest {

	@Test
	void acceptsSingleActiveQuerySelectListStep() throws Exception {
		FmDataAction action = action("QUERY");
		FmDataActionStep step = step("ACTIVE", "SELECT_LIST", "ONCE", 0);

		assertSame(step, FmDataActionExecutor.requireStreamingStep(
				action, List.of(step)));
	}

	@Test
	void rejectsMutationAndMultipleActiveSteps() {
		FmDataActionStep step = step("ACTIVE", "SELECT_LIST", "ONCE", 0);

		assertThrows(ServiceException.class, () ->
				FmDataActionExecutor.requireStreamingStep(
						action("COMMAND"), List.of(step)));
		assertThrows(ServiceException.class, () ->
				FmDataActionExecutor.requireStreamingStep(
						action("QUERY"), List.of(step, step("ACTIVE",
							"SELECT_LIST", "ONCE", 0))));
	}

	@Test
	void rejectsSelectOneForEachAndRetry() {
		FmDataAction action = action("QUERY");

		assertThrows(ServiceException.class, () ->
				FmDataActionExecutor.requireStreamingStep(action,
						List.of(step("ACTIVE", "SELECT_ONE", "ONCE", 0))));
		assertThrows(ServiceException.class, () ->
				FmDataActionExecutor.requireStreamingStep(action,
						List.of(step("ACTIVE", "SELECT_LIST", "FOR_EACH", 0))));
		assertThrows(ServiceException.class, () ->
				FmDataActionExecutor.requireStreamingStep(action,
						List.of(step("ACTIVE", "SELECT_LIST", "ONCE", 1))));
	}

	private FmDataAction action(String type) {
		FmDataAction action = new FmDataAction();
		action.setActionType(type);
		return action;
	}

	private FmDataActionStep step(String status, String statementType,
			String executionMode, int retryCount) {
		FmDataActionStep step = new FmDataActionStep();
		step.setStatus(status);
		step.setStatementType(statementType);
		step.setExecutionMode(executionMode);
		step.setRetryCount(retryCount);
		return step;
	}
}
