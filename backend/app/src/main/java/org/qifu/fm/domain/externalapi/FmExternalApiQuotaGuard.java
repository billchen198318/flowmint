package org.qifu.fm.domain.externalapi;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.service.IFmApiAccessLogService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FmExternalApiQuotaGuard {

	private final IFmApiAccessLogService accessLogService;
	private final ZoneId quotaZone;

	public FmExternalApiQuotaGuard(IFmApiAccessLogService accessLogService,
			Environment environment) {
		this.accessLogService = accessLogService;
		this.quotaZone = ZoneId.of(environment.getProperty(
				"fm.external-api.quota-zone", "Asia/Taipei"));
	}

	public void requireAvailable(FmExternalApiPrincipal principal)
			throws ServiceException {
		Instant now = Instant.now();
		Date minuteStart = Date.from(now.truncatedTo(ChronoUnit.MINUTES));
		ZonedDateTime localNow = now.atZone(quotaZone);
		Date dayStart = Date.from(localNow.toLocalDate().atStartOfDay(quotaZone)
				.toInstant());
		long minuteCount = accessLogService.countClientRequestsSince(
				principal.tenantId(), principal.clientId(), minuteStart);
		if (minuteCount >= principal.rateLimitPerMinute()) {
			throw new ServiceException("External API rate limit exceeded.");
		}
		long dayCount = accessLogService.countClientRequestsSince(
				principal.tenantId(), principal.clientId(), dayStart);
		if (dayCount >= principal.dailyQuota()) {
			throw new ServiceException("External API daily quota exceeded.");
		}
	}
}
