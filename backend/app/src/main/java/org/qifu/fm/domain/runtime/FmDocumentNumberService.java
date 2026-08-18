package org.qifu.fm.domain.runtime;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmDocumentNumberRule;
import org.qifu.fm.entity.FmDocumentSequence;
import org.qifu.fm.service.IFmDocumentNumberRuleService;
import org.qifu.fm.service.IFmDocumentSequenceService;
import org.springframework.stereotype.Service;

@Service
public class FmDocumentNumberService {

    private static final DateTimeFormatter YEAR = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM");

    private final IFmDocumentNumberRuleService ruleService;
    private final IFmDocumentSequenceService sequenceService;

    public FmDocumentNumberService(
            IFmDocumentNumberRuleService ruleService,
            IFmDocumentSequenceService sequenceService) {
        this.ruleService = ruleService;
        this.sequenceService = sequenceService;
    }

    public String nextNumber(
            String tenantId,
            String tenantCode,
            String tenantTimezone,
            String documentType,
            String account,
            Date now) throws ServiceException {
        if (StringUtils.isAnyBlank(
                tenantId, tenantCode, documentType, account) || now == null) {
            throw new ServiceException("單據取號參數不完整");
        }
        FmDocumentNumberRule rule = ruleService.selectActive(tenantId, documentType);
        if (rule == null) {
            throw new ServiceException("找不到啟用的單據編號規則：" + documentType);
        }
        validateRule(rule);
        ZonedDateTime localTime = Instant.ofEpochMilli(now.getTime())
                .atZone(zoneId(tenantTimezone));
        String periodKey = periodKey(rule.getPeriodType(), localTime);
        FmDocumentSequence sequence = lockedSequence(
                tenantId, documentType, periodKey, account, now);
        if (sequenceService.increment(
                tenantId, sequence.getOid(), sequence.getLockVersion(), account, now) != 1) {
            throw new ServiceException("單據流水號已被其他交易更新，請重新送出");
        }
        long next = sequence.getCurrentNo() + 1;
        long maximum = maximum(rule.getSequenceLength());
        if (next > maximum) {
            throw new ServiceException("單據流水號已超過設定長度：" + documentType);
        }
        return format(rule, tenantCode, localTime, next);
    }

    private FmDocumentSequence lockedSequence(
            String tenantId,
            String documentType,
            String periodKey,
            String account,
            Date now) throws ServiceException {
        FmDocumentSequence value = sequenceService.selectForUpdate(
                tenantId, documentType, periodKey);
        if (value != null) {
            return value;
        }
        FmDocumentSequence initial = new FmDocumentSequence();
        initial.setOid(UUID.randomUUID().toString());
        initial.setTenantId(tenantId);
        initial.setDocumentType(documentType);
        initial.setPeriodKey(periodKey);
        initial.setCurrentNo(0L);
        initial.setLockVersion(0L);
        initial.setCuserid(account);
        initial.setCdate(now);
        sequenceService.insertInitial(initial);
        value = sequenceService.selectForUpdate(tenantId, documentType, periodKey);
        if (value == null) {
            throw new ServiceException("無法建立或鎖定單據流水號");
        }
        return value;
    }

    private void validateRule(FmDocumentNumberRule rule) throws ServiceException {
        if (StringUtils.isAnyBlank(
                rule.getPrefix(), rule.getPeriodType(), rule.getFormatPattern())
                || rule.getSequenceLength() == null
                || rule.getSequenceLength() < 4
                || rule.getSequenceLength() > 12) {
            throw new ServiceException("單據編號規則不完整：" + rule.getDocumentType());
        }
        if (!rule.getFormatPattern().contains("{SEQ}")) {
            throw new ServiceException("單據編號格式缺少 {SEQ}：" + rule.getDocumentType());
        }
    }

    private ZoneId zoneId(String timezone) throws ServiceException {
        try {
            return ZoneId.of(StringUtils.defaultIfBlank(timezone, "UTC"));
        } catch (DateTimeException exception) {
            throw new ServiceException("Tenant 時區無效：" + timezone);
        }
    }

    private String periodKey(String periodType, ZonedDateTime time)
            throws ServiceException {
        return switch (periodType) {
            case "NONE" -> "NONE";
            case "YEAR" -> time.format(YEAR);
            case "MONTH" -> time.format(YEAR) + time.format(MONTH);
            default -> throw new ServiceException("不支援的單據計號週期：" + periodType);
        };
    }

    private String format(
            FmDocumentNumberRule rule,
            String tenantCode,
            ZonedDateTime time,
            long sequence) throws ServiceException {
        String value = rule.getFormatPattern()
                .replace("{PREFIX}", rule.getPrefix())
                .replace("{TENANT}", tenantCode)
                .replace("{YYYY}", time.format(YEAR))
                .replace("{MM}", time.format(MONTH))
                .replace("{SEQ}", String.format(
                        "%0" + rule.getSequenceLength() + "d", sequence));
        if (value.contains("{") || value.contains("}")) {
            throw new ServiceException("單據編號格式包含不支援的 token");
        }
        if (value.length() > 100) {
            throw new ServiceException("產生的單據編號超過 100 字元");
        }
        return value;
    }

    private long maximum(int length) {
        long value = 1;
        for (int index = 0; index < length; index++) {
            value *= 10;
        }
        return value - 1;
    }
}
