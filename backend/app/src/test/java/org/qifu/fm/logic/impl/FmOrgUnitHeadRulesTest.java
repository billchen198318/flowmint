package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmOrgUnitHeadCommand;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTenantService;

class FmOrgUnitHeadRulesTest {
	@Test
	void employeeOptionsExcludeFutureAndExpiredEmployment() throws Exception {
		Fixture fixture = new Fixture();
		long now = System.currentTimeMillis();
		FmEmployeeOrgAssignment current = assignment("CURRENT", now - 86_400_000L, null);
		FmEmployeeOrgAssignment future = assignment("FUTURE", now + 86_400_000L, null);
		FmEmployeeOrgAssignment expired = assignment("EXPIRED", 0, now - 86_400_000L);
		when(fixture.assignments.selectListByParams(anyMap(), anyString(), anyString()))
				.thenReturn(result(List.of(current, future, expired)));
		when(fixture.employees.selectListByParams(anyMap(), anyString(), anyString()))
				.thenReturn(result(List.of(employee("CURRENT", now - 86_400_000L, null),
						employee("FUTURE", 0, null), employee("EXPIRED", 0, null))));

		assertEquals(List.of("CURRENT"), fixture.logic.employeeOptions("T", "ORG").getValue()
				.stream().map(value -> value.value()).toList());
	}

	@Test
	void createRejectsAssignmentThatEndsBeforeHeadTerm() throws Exception {
		Fixture fixture = new Fixture();
		long now = System.currentTimeMillis();
		FmOrgUnitView unit = new FmOrgUnitView();
		unit.setOrgUnitId("ORG");
		unit.setUnitCode("ORG");
		unit.setUnitName("Organization");
		when(fixture.units.selectCurrentTree(anyMap())).thenReturn(result(List.of(unit)));
		when(fixture.assignments.selectListByParams(anyMap(), anyString(), anyString()))
				.thenReturn(result(List.of(assignment("E", now - 1_000L, now + 86_400_000L))));
		when(fixture.employees.selectListByParams(anyMap(), anyString(), anyString()))
				.thenReturn(result(List.of(employee("E", now - 1_000L, null))));
		FmOrgUnitHeadCommand command = new FmOrgUnitHeadCommand(null, "T", "ORG", "E", "HEAD",
				1, "ACTIVE", new Date(now), new Date(now + 2 * 86_400_000L), null);

		assertThrows(ServiceException.class, () -> fixture.logic.create(command));
	}

	@Test
	void futureHeadCanBeDeactivatedWithoutInvalidEndDate() throws Exception {
		Fixture fixture = new Fixture();
		FmOrgUnitHead head = new FmOrgUnitHead();
		head.setTenantId("T");
		head.setEffectiveFrom(new Date(System.currentTimeMillis() + 86_400_000L));
		when(fixture.heads.selectByPrimaryKey("H")).thenReturn(result(head));
		doAnswer(invocation -> {
			assertEquals("INACTIVE", head.getStatus());
			assertNull(head.getEffectiveTo());
			throw new ServiceException("STOP AFTER UPDATE");
		}).when(fixture.heads).update(head);

		assertThrows(ServiceException.class, () -> fixture.logic.deactivate("H"));
	}

	@Test
	void loadRejectsOtherTenantBeforeResolvingLabels() throws Exception {
		Fixture fixture = new Fixture();
		FmOrgUnitHead head = new FmOrgUnitHead();
		head.setTenantId("OTHER");
		when(fixture.heads.selectByPrimaryKey("H")).thenReturn(result(head));
		doThrow(new ServiceException("DENIED")).when(fixture.access).requireAccess("OTHER");

		assertThrows(ServiceException.class, () -> fixture.logic.load("H", null));
		verifyNoInteractions(fixture.assignments, fixture.employees);
	}

	private static FmEmployeeOrgAssignment assignment(String id, long from, Long to) {
		FmEmployeeOrgAssignment assignment = new FmEmployeeOrgAssignment();
		assignment.setEmployeeId(id);
		assignment.setStatus("ACTIVE");
		assignment.setEffectiveFrom(new Date(from));
		assignment.setEffectiveTo(to == null ? null : new Date(to));
		return assignment;
	}

	private static FmEmployee employee(String id, long from, Long to) {
		FmEmployee employee = new FmEmployee();
		employee.setEmployeeId(id);
		employee.setEmployeeNo(id);
		employee.setDisplayName(id);
		employee.setStatus("ACTIVE");
		employee.setEffectiveFrom(new Date(from));
		employee.setEffectiveTo(to == null ? null : new Date(to));
		return employee;
	}

	private static <T> DefaultResult<T> result(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setValue(value);
		return result;
	}

	private static class Fixture {
		final IFmOrgUnitHeadService heads = mock(IFmOrgUnitHeadService.class);
		final IFmEmployeeOrgAssignmentService assignments = mock(IFmEmployeeOrgAssignmentService.class);
		final IFmEmployeeService employees = mock(IFmEmployeeService.class);
		final IFmOrgUnitVersionService units = mock(IFmOrgUnitVersionService.class);
		final FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
		final FmOrgUnitHeadLogicServiceImpl logic = new FmOrgUnitHeadLogicServiceImpl(
				heads, units, assignments, employees,
				mock(IFmTenantService.class), access);
	}
}
