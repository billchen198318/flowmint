package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.qifu.base.exception.ServiceException;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmUserProcessHistoryRequest;
import org.qifu.fm.logic.IFmRequestTrackingLogicService;
import org.qifu.fm.model.FmUserProcessHistoryRow;
import org.qifu.fm.service.IFmProcessInstanceService;

class FmUserProcessHistoryLogicServiceImplTest {

    @Test
    void usesAuthenticatedAccountAndCombinesRelationsWithoutDuplicates() throws Exception {
        FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
        IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
        FmUserProcessHistoryRow row = new FmUserProcessHistoryRow();
        row.setProcessInstanceId("P-1");
        row.setApplicant(true);
        row.setStarter(true);
        row.setApprover(true);
        row.setInstanceStatus("COMPLETED");
        row.setStartDate(new Date());
        when(processes.countUserHistory(anyMap())).thenReturn(1L);
        when(processes.userHistory(anyMap())).thenReturn(List.of(row));
        FmUserProcessHistoryLogicServiceImpl logic = new FmUserProcessHistoryLogicServiceImpl(
                access, processes, mock(IFmRequestTrackingLogicService.class),
                mock(TaskService.class));
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("common-user");

        try (MockedStatic<UserUtils> users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::getCurrentUser).thenReturn(user);
            var result = logic.findPage("T-1", new FmUserProcessHistoryRequest(
                    "ALL", null, null, "PR", null, null, null, null, 1, 20));

            assertEquals(1L, result.getValue().totalCount());
            assertEquals(List.of("APPLICANT", "STARTER", "APPROVER"),
                    result.getValue().items().getFirst().relations());
            ArgumentCaptor<Map<String, Object>> parameters = ArgumentCaptor.forClass(Map.class);
            verify(processes).countUserHistory(parameters.capture());
            assertEquals("common-user", parameters.getValue().get("account"));
            assertEquals("T-1", parameters.getValue().get("tenantId"));
            assertEquals("%PR%", parameters.getValue().get("keywordLike"));
            assertEquals(0, parameters.getValue().get("offset"));
            assertEquals(20, parameters.getValue().get("limit"));
            verify(access).requireAccess("T-1");
        }
    }

    @Test
    void rejectsUnsupportedRelationBeforeQuerying() {
        FmUserProcessHistoryLogicServiceImpl logic = new FmUserProcessHistoryLogicServiceImpl(
                mock(FmTenantAccessGuard.class), mock(IFmProcessInstanceService.class),
                mock(IFmRequestTrackingLogicService.class), mock(TaskService.class));
        assertThrows(ServiceException.class, () -> logic.findPage("T-1",
                new FmUserProcessHistoryRequest(
                        "OTHER_USER", null, null, null,
                        null, null, null, null, 1, 20)));
    }
}
