package org.qifu.fm.domain.resolver;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmOrgUnitHead;

class FmFormalHeadSelectionTest {
	@Test
	void skipsDeputyAndActingHeadsEvenWhenTheyComeFirst() {
		Date now = new Date(100);
		FmOrgUnitHead deputy = head("DEPUTY_HEAD", 1);
		FmOrgUnitHead acting = head("ACTING_HEAD", 2);
		FmOrgUnitHead formal = head("HEAD", 3);

		assertSame(formal, FmAssignmentResolverService.firstEffectiveFormalHead(
				List.of(deputy, acting, formal), now));
	}

	private static FmOrgUnitHead head(String type, int priority) {
		FmOrgUnitHead head = new FmOrgUnitHead();
		head.setHeadType(type);
		head.setPriority(priority);
		head.setStatus("ACTIVE");
		head.setEffectiveFrom(new Date(0));
		return head;
	}
}
