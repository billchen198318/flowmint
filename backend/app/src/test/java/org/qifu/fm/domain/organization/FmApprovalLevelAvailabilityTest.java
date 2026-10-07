package org.qifu.fm.domain.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgLevelScheme;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgLevelSchemeService;

class FmApprovalLevelAvailabilityTest {
	@Test
	void excludesLevelsOutsideTitlePeriodAndInactiveSchemes() throws Exception {
		IFmOrgApprovalLevelService levels = mock(IFmOrgApprovalLevelService.class);
		IFmOrgLevelSchemeService schemes = mock(IFmOrgLevelSchemeService.class);
		FmOrgApprovalLevel valid = level("VALID", "S1", 0, 200);
		FmOrgApprovalLevel expired = level("EXPIRED", "S1", 0, 60);
		FmOrgApprovalLevel future = level("FUTURE", "S1", 70, 200);
		FmOrgApprovalLevel inactiveSchemeLevel = level("INACTIVE_SCHEME", "S2", 0, 200);
		when(levels.selectListByParams(anyMap(), eq("LEVEL_ORDER"), eq("ASC")))
				.thenReturn(result(List.of(valid, expired, future, inactiveSchemeLevel)));
		when(schemes.selectListByParams(anyMap()))
				.thenReturn(result(List.of(scheme("S1", "ACTIVE"), scheme("S2", "INACTIVE"))));

		List<FmOrgApprovalLevel> available = new FmApprovalLevelAvailability(levels, schemes)
				.availableDuring("T", new Date(50), new Date(100));

		assertEquals(List.of("VALID"), available.stream().map(FmOrgApprovalLevel::getApprovalLevelId).toList());
	}

	private static FmOrgApprovalLevel level(String id, String schemeId, long from, long to) {
		FmOrgApprovalLevel level = new FmOrgApprovalLevel();
		level.setApprovalLevelId(id);
		level.setLevelSchemeId(schemeId);
		level.setStatus("ACTIVE");
		level.setEffectiveFrom(new Date(from));
		level.setEffectiveTo(new Date(to));
		return level;
	}

	private static FmOrgLevelScheme scheme(String id, String status) {
		FmOrgLevelScheme scheme = new FmOrgLevelScheme();
		scheme.setLevelSchemeId(id);
		scheme.setStatus(status);
		scheme.setEffectiveFrom(new Date(0));
		scheme.setEffectiveTo(new Date(200));
		return scheme;
	}

	private static <T> DefaultResult<T> result(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setValue(value);
		return result;
	}
}
