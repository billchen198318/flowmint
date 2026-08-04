package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmDataActionStep;

class FmDataActionSqlValidatorTest {

	private final FmDataActionSqlValidator validator =
			new FmDataActionSqlValidator();

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

	private FmDataActionStep selectStep(String sql) {
		FmDataActionStep step = new FmDataActionStep();
		step.setStatementType("SELECT_LIST");
		step.setExecutionMode("ONCE");
		step.setSqlContent(sql);
		return step;
	}
}
