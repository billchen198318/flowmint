package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmDocumentNumberRule;
import org.qifu.fm.entity.FmDocumentSequence;
import org.qifu.fm.service.IFmDocumentNumberRuleService;
import org.qifu.fm.service.IFmDocumentSequenceService;

class FmDocumentNumberServiceTest {

    @Test
    void generatesMonthlyTenantNumberAndIncrementsLockedRow() throws Exception {
        IFmDocumentNumberRuleService rules = mock(IFmDocumentNumberRuleService.class);
        IFmDocumentSequenceService sequences = mock(IFmDocumentSequenceService.class);
        FmDocumentNumberRule rule = rule();
        FmDocumentSequence sequence = sequence(41L, 7L);
        when(rules.selectActive("A01", "PURCHASE_REQUEST")).thenReturn(rule);
        when(sequences.selectForUpdate(
                "A01", "PURCHASE_REQUEST", "202609")).thenReturn(sequence);
        when(sequences.increment(
                eq("A01"), eq("SEQ-OID"), eq(7L), eq("tester"), any()))
                .thenReturn(1);

        String value = new FmDocumentNumberService(rules, sequences).nextNumber(
                "A01",
                "A01",
                "Asia/Taipei",
                "PURCHASE_REQUEST",
                "tester",
                Date.from(Instant.parse("2026-08-31T16:30:00Z")));

        assertEquals("PR-A01-202609-000042", value);
        verify(sequences).increment(
                eq("A01"), eq("SEQ-OID"), eq(7L), eq("tester"), any());
    }

    @Test
    void rejectsMissingActiveRule() {
        IFmDocumentNumberRuleService rules = mock(IFmDocumentNumberRuleService.class);
        IFmDocumentSequenceService sequences = mock(IFmDocumentSequenceService.class);
        when(rules.selectActive("A01", "PURCHASE_REQUEST")).thenReturn(null);

        assertThrows(ServiceException.class, () ->
                new FmDocumentNumberService(rules, sequences).nextNumber(
                        "A01", "A01", "Asia/Taipei", "PURCHASE_REQUEST",
                        "tester", new Date()));
    }

    private FmDocumentNumberRule rule() {
        FmDocumentNumberRule value = new FmDocumentNumberRule();
        value.setDocumentType("PURCHASE_REQUEST");
        value.setPrefix("PR");
        value.setPeriodType("MONTH");
        value.setSequenceLength(6);
        value.setFormatPattern("{PREFIX}-{TENANT}-{YYYY}{MM}-{SEQ}");
        return value;
    }

    private FmDocumentSequence sequence(long currentNo, long lockVersion) {
        FmDocumentSequence value = new FmDocumentSequence();
        value.setOid("SEQ-OID");
        value.setCurrentNo(currentNo);
        value.setLockVersion(lockVersion);
        return value;
    }
}
