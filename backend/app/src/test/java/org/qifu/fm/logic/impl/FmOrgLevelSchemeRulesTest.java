package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmOrgLevelCommand;
import org.qifu.fm.dto.command.FmOrgLevelSchemeCommand;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgLevelScheme;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgLevelSchemeService;
import org.qifu.fm.service.IFmTenantService;

class FmOrgLevelSchemeRulesTest {
	private static final Date FROM = new Date(1_000L);

	@Test
	void swappingOrdersParksBothRowsBeforeFinalValues() throws Exception {
		Fixture fixture = new Fixture();
		FmOrgApprovalLevel a = level("A", "A", 10, FROM);
		FmOrgApprovalLevel b = level("B", "B", 20, FROM);
		fixture.levels(List.of(a, b));
		List<String> writes = new ArrayList<>();
		doAnswer(invocation -> {
			FmOrgApprovalLevel value = invocation.getArgument(0);
			writes.add(value.getOid() + ":" + value.getLevelOrder());
			return null;
		}).when(fixture.levelService).update(org.mockito.ArgumentMatchers.any());

		fixture.logic.update(command(List.of(commandLevel("A", "A", 20, "Y"),
				commandLevel("B", "B", 10, "N"))));

		assertEquals(4, writes.size());
		assertTrue(writes.get(0).startsWith("A:2147483647"));
		assertTrue(writes.get(1).startsWith("B:2147483646"));
		assertEquals("A:20", writes.get(2));
		assertEquals("B:10", writes.get(3));
	}

	@Test
	void removedFutureLevelKeepsValidPeriod() throws Exception {
		Fixture fixture = new Fixture();
		FmOrgApprovalLevel active = level("A", "A", 10, FROM);
		FmOrgApprovalLevel future = level("B", "B", 20, new Date(System.currentTimeMillis() + 86_400_000L));
		fixture.levels(List.of(active, future));

		fixture.logic.update(command(List.of(commandLevel("A", "A", 10, "Y"))));

		assertEquals("INACTIVE", future.getStatus());
		assertEquals(null, future.getEffectiveTo());
	}

	@Test
	void replacingRemovedLevelWithReservedCodeIsRejectedClearly() throws Exception {
		Fixture fixture = new Fixture();
		fixture.levels(List.of(level("A", "A", 10, FROM), level("B", "B", 20, FROM)));
		FmOrgLevelCommand replacement = new FmOrgLevelCommand(null, "B", "Replacement", 30,
				"N", "ACTIVE", FROM, null, null);

		ServiceException error = assertThrows(ServiceException.class, () -> fixture.logic.update(
				command(List.of(commandLevel("A", "A", 10, "Y"), replacement))));

		assertTrue(error.getMessage().contains("不可重用"));
	}

	@Test
	void deactivatingSchemeDeactivatesItsLevels() throws Exception {
		Fixture fixture = new Fixture();
		FmOrgApprovalLevel active = level("A", "A", 10, FROM);
		fixture.levels(List.of(active));

		fixture.logic.deactivate("S");

		assertEquals("INACTIVE", fixture.scheme.getStatus());
		assertEquals("INACTIVE", active.getStatus());
	}

	@Test
	void loadRejectsInaccessibleTenantBeforeReadingLevels() throws Exception {
		Fixture fixture = new Fixture();
		doThrow(new ServiceException("DENIED")).when(fixture.access).requireAccess("T");

		assertThrows(ServiceException.class, () -> fixture.logic.load("S", null));
		verifyNoInteractions(fixture.levelService);
	}

	private static FmOrgLevelSchemeCommand command(List<FmOrgLevelCommand> levels) {
		return new FmOrgLevelSchemeCommand("S", "T", "SC", "Scheme", "N", "ACTIVE",
				FROM, null, null, levels);
	}

	private static FmOrgLevelCommand commandLevel(String oid, String code, int order, String highest) {
		return new FmOrgLevelCommand(oid, code, code, order, highest, "ACTIVE", FROM, null, null);
	}

	private static FmOrgApprovalLevel level(String oid, String code, int order, Date from) {
		FmOrgApprovalLevel level = new FmOrgApprovalLevel();
		level.setOid(oid);
		level.setTenantId("T");
		level.setLevelSchemeId("SC");
		level.setLevelCode(code);
		level.setLevelName(code);
		level.setLevelOrder(order);
		level.setStatus("ACTIVE");
		level.setEffectiveFrom(from);
		return level;
	}

	private static <T> DefaultResult<T> result(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setValue(value);
		return result;
	}

	private static class Fixture {
		final IFmOrgLevelSchemeService schemeService = mock(IFmOrgLevelSchemeService.class);
		final IFmOrgApprovalLevelService levelService = mock(IFmOrgApprovalLevelService.class);
		final FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
		final FmOrgLevelScheme scheme = new FmOrgLevelScheme();
		final FmOrgLevelSchemeLogicServiceImpl logic = new FmOrgLevelSchemeLogicServiceImpl(
				schemeService, levelService, mock(IFmTenantService.class), access);

		Fixture() throws Exception {
			scheme.setOid("S");
			scheme.setTenantId("T");
			scheme.setLevelSchemeId("SC");
			scheme.setStatus("ACTIVE");
			when(schemeService.selectByPrimaryKey("S")).thenReturn(result(scheme));
		}

		void levels(List<FmOrgApprovalLevel> values) throws Exception {
			when(levelService.selectListByParams(anyMap(), eq("LEVEL_ORDER"), eq("ASC")))
					.thenReturn(result(values));
		}
	}
}
