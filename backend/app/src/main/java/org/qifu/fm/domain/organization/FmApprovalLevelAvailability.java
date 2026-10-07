package org.qifu.fm.domain.organization;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgLevelScheme;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgLevelSchemeService;
import org.springframework.stereotype.Component;

@Component
public class FmApprovalLevelAvailability {
	private final IFmOrgApprovalLevelService levels;
	private final IFmOrgLevelSchemeService schemes;

	public FmApprovalLevelAvailability(IFmOrgApprovalLevelService levels,
			IFmOrgLevelSchemeService schemes) {
		this.levels = levels;
		this.schemes = schemes;
	}

	public List<FmOrgApprovalLevel> availableDuring(String tenantId, Date from, Date to)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("status", "ACTIVE");
		Map<String, FmOrgLevelScheme> activeSchemes = schemes.selectListByParams(parameters)
				.getValue().stream().filter(scheme -> "ACTIVE".equals(scheme.getStatus()))
				.collect(Collectors.toMap(
						FmOrgLevelScheme::getLevelSchemeId, Function.identity()));
		return levels.selectListByParams(parameters, "LEVEL_ORDER", "ASC").getValue().stream()
				.filter(level -> "ACTIVE".equals(level.getStatus()))
				.filter(level -> covers(level.getEffectiveFrom(), level.getEffectiveTo(), from, to))
				.filter(level -> {
					FmOrgLevelScheme scheme = activeSchemes.get(level.getLevelSchemeId());
					return scheme != null && covers(scheme.getEffectiveFrom(), scheme.getEffectiveTo(), from, to);
				})
				.toList();
	}

	private boolean covers(Date effectiveFrom, Date effectiveTo, Date from, Date to) {
		return effectiveFrom != null && !effectiveFrom.after(from)
				&& (effectiveTo == null || effectiveTo.after(from))
				&& (to != null ? effectiveTo == null || !effectiveTo.before(to) : effectiveTo == null);
	}
}
