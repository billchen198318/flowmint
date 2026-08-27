package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmDataActionStep;

class FmDataActionSqlValidatorTest {

	private final FmDataActionSqlValidator validator =
			new FmDataActionSqlValidator(new FmDataActionContinueConditionEvaluator(
					new FmDataActionParameterResolver(new tools.jackson.databind.ObjectMapper())));

	@Test
	void acceptsNamedParametersWithDefinedSources() {
		FmDataActionStep step = selectStep(
				"SELECT EMPLOYEE_ID FROM fm_employee "
						+ "WHERE TENANT_ID = :tenantId AND ACCOUNT = :account");
		assertDoesNotThrow(() -> validator.validate(
				step, "QUERY", Set.of("tenantId", "account")));
	}

	@Test
	void rejectsMultipleStatementsAndStringSubstitution() {
		assertThrows(ServiceException.class, () -> validator.validate(
				selectStep("SELECT 1; DELETE FROM fm_employee"),
				"QUERY", Set.of()));
		assertThrows(ServiceException.class, () -> validator.validate(
				selectStep("SELECT ${columnName} FROM fm_employee"),
				"QUERY", Set.of("columnName")));
	}

	@Test
	void rejectsMutationStepForQueryAction() {
		FmDataActionStep step = selectStep(
				"UPDATE fm_employee SET STATUS = :status");
		step.setStatementType("UPDATE");
		assertThrows(ServiceException.class, () -> validator.validate(
				step, "QUERY", Set.of("status")));
	}

	@Test
	void acceptsForEachMutationWithArrayPath() {
		FmDataActionStep step = selectStep(
				"INSERT INTO request_item (ITEM_CODE) VALUES (:itemCode)");
		step.setStatementType("INSERT");
		step.setExecutionMode("FOR_EACH");
		step.setArrayPath("$.items");
		assertDoesNotThrow(() -> validator.validate(
				step, "TRANSACTION", Set.of("itemCode")));
	}

	@Test
	void rejectsForEachWithoutArrayPathAndGeneratedKeyOnUpdate() {
		FmDataActionStep step = selectStep(
				"INSERT INTO request_item (ITEM_CODE) VALUES (:itemCode)");
		step.setStatementType("INSERT");
		step.setExecutionMode("FOR_EACH");
		assertThrows(ServiceException.class, () -> validator.validate(
				step, "TRANSACTION", Set.of("itemCode")));

		step.setExecutionMode("ONCE");
		step.setStatementType("UPDATE");
		step.setSqlContent("UPDATE request_item SET ITEM_CODE = :itemCode");
		step.setResultMode("GENERATED_KEY");
		assertThrows(ServiceException.class, () -> validator.validate(
				step, "TRANSACTION", Set.of("itemCode")));
	}

	@Test
	void acceptsRetryForQuerySelectAndRejectsMutationRetry() {
		FmDataActionStep query = selectStep("SELECT 1 AS VALUE");
		query.setRetryCount(2);
		query.setRetryDelayMillis(100);
		assertDoesNotThrow(() -> validator.validate(query, "QUERY", Set.of()));

		FmDataActionStep mutation = selectStep(
				"UPDATE request_item SET ITEM_CODE = :itemCode");
		mutation.setStatementType("UPDATE");
		mutation.setRetryCount(1);
		assertThrows(ServiceException.class, () -> validator.validate(
				mutation, "TRANSACTION", Set.of("itemCode")));
	}

	private FmDataActionStep selectStep(String sql) {
		FmDataActionStep step = new FmDataActionStep();
		step.setStatementType("SELECT_LIST");
		step.setExecutionMode("ONCE");
		step.setSqlContent(sql);
		return step;
	}
}
