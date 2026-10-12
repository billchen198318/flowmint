package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmWorkflowDelegation;

class FmOrganizationHealthDelegationCycleTest {

	private final FmOrganizationHealthLogicServiceImpl logic =
			new FmOrganizationHealthLogicServiceImpl(null, null, null, null, null, null);

	@Test
	void retainsParallelEdgesAndRequiresCommonPeriodAndScope() {
		List<FmWorkflowDelegation> delegations = List.of(
				edge("A", "B", "PROCESS", "other", 0, 100),
				edge("A", "B", "PROCESS", "target", 0, 100),
				edge("B", "C", "ALL", null, 20, 90),
				edge("C", "A", "PROCESS", "target", 30, 80));
		assertEquals(1, logic.delegationCycleIssues(delegations, Map.of()).size());

		assertEquals(0, logic.delegationCycleIssues(List.of(
				edge("A", "B", "PROCESS", "first", 0, 100),
				edge("B", "C", "ALL", null, 0, 100),
				edge("C", "A", "PROCESS", "second", 0, 100)), Map.of()).size());
		assertEquals(0, logic.delegationCycleIssues(List.of(
				edge("A", "B", "ALL", null, 0, 40),
				edge("B", "C", "ALL", null, 30, 70),
				edge("C", "A", "ALL", null, 50, 100)), Map.of()).size());
	}

	private FmWorkflowDelegation edge(String principal, String delegate,
			String scopeType, String scopeRefId, long from, long to) {
		FmWorkflowDelegation value = new FmWorkflowDelegation();
		value.setPrincipalAccount(principal);
		value.setDelegateAccount(delegate);
		value.setScopeType(scopeType);
		value.setScopeRefId(scopeRefId);
		value.setEffectiveFrom(new Date(from));
		value.setEffectiveTo(new Date(to));
		return value;
	}
}
