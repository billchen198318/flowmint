package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FmTaskPolicySlaValidationTest {
	@Test
	void rejectsZeroHourReminderThatCannotFireBeforeDeadline() {
		assertFalse(FmProcessDefLogicServiceImpl.validSla(8, 0));
		assertTrue(FmProcessDefLogicServiceImpl.validSla(8, 1));
		assertTrue(FmProcessDefLogicServiceImpl.validSla(8, null));
		assertFalse(FmProcessDefLogicServiceImpl.validSla(1, 1));
	}
}
