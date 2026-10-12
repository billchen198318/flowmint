package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.command.FmWorkflowDelegationCommand;
import org.qifu.fm.mapper.FmWorkflowDelegationMapper;

class FmWorkflowDelegationOverlapTest {

	@Test
	void locksTenantBeforeCheckingCurrentOverlappingRows() throws ServiceException {
		FmWorkflowDelegationMapper mapper = mock(FmWorkflowDelegationMapper.class);
		FmWorkflowDelegationLogicServiceImpl logic = new FmWorkflowDelegationLogicServiceImpl(
				null, null, null, null, null, null, null, mapper);
		FmWorkflowDelegationCommand command = new FmWorkflowDelegationCommand(
				null, "tenant", "principal", "delegate", "ALL", null,
				"N", "ACTIVE", new Date(10), new Date(20), "reason");
		when(mapper.lockTenantForWrite("tenant")).thenReturn("tenant");
		when(mapper.selectOverlappingActiveForUpdate("tenant", "principal", "ALL",
				null, command.effectiveFrom(), command.effectiveTo(), null))
				.thenReturn(List.of("existing"));

		assertThrows(ServiceException.class, () -> logic.validateOverlap(command, null));
		InOrder order = inOrder(mapper);
		order.verify(mapper).lockTenantForWrite("tenant");
		order.verify(mapper).selectOverlappingActiveForUpdate("tenant", "principal", "ALL",
				null, command.effectiveFrom(), command.effectiveTo(), null);
	}
}
