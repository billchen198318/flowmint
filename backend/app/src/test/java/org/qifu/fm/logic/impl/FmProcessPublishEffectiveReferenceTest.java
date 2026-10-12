package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgDuty;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgDutyService;
import org.qifu.fm.service.IFmOrgTitleService;

class FmProcessPublishEffectiveReferenceTest {

    @Test
    void activeStatusWithoutEffectiveDateIsRejected() throws Exception {
        IFmOrgApprovalLevelService levels = mock(IFmOrgApprovalLevelService.class);
        IFmOrgTitleService titles = mock(IFmOrgTitleService.class);
        IFmOrgDutyService duties = mock(IFmOrgDutyService.class);
        FmProcessDefLogicServiceImpl logic = new FmProcessDefLogicServiceImpl(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, levels, titles, duties, null, null, null, null,
                null, null, null, null, null, false, null);
        Date expired = Date.from(Instant.now().minusSeconds(60));
        FmOrgApprovalLevel level = new FmOrgApprovalLevel();
        level.setEffectiveTo(expired);
        FmOrgTitle title = new FmOrgTitle();
        title.setEffectiveTo(expired);
        FmOrgDuty duty = new FmOrgDuty();
        duty.setEffectiveTo(expired);
        when(levels.selectListByParams(anyMap())).thenReturn(result(List.of(level)));
        when(titles.selectListByParams(anyMap())).thenReturn(result(List.of(title)));
        when(duties.selectListByParams(anyMap())).thenReturn(result(List.of(duty)));

        assertFalse(logic.validateActiveApprovalLevel("tenant", "level"));
        assertFalse(logic.validateActiveOrgTitle("tenant", "title"));
        assertFalse(logic.validateActiveOrgDuty("tenant", "duty"));

        level.setEffectiveTo(null);
        title.setEffectiveTo(null);
        duty.setEffectiveTo(null);
        assertTrue(logic.validateActiveApprovalLevel("tenant", "level"));
        assertTrue(logic.validateActiveOrgTitle("tenant", "title"));
        assertTrue(logic.validateActiveOrgDuty("tenant", "duty"));
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
