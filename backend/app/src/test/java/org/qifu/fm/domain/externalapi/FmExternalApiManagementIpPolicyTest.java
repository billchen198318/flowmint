package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmApiClientCommand;
import org.qifu.fm.logic.impl.FmExternalApiManagementLogicServiceImpl;
import org.qifu.fm.service.IFmApiClientKeyService;
import org.qifu.fm.service.IFmApiClientService;
import org.qifu.fm.service.IFmTenantService;

import tools.jackson.databind.ObjectMapper;

class FmExternalApiManagementIpPolicyTest {
    @Test
    void rejectsHostnameBeforeSavingClient() {
        IFmApiClientService clients = mock(IFmApiClientService.class);
        FmExternalApiManagementLogicServiceImpl logic = new FmExternalApiManagementLogicServiceImpl(
                clients, mock(IFmApiClientKeyService.class),
                mock(FmExternalApiKeyService.class), mock(FmTenantAccessGuard.class),
                mock(IFmTenantService.class), new ObjectMapper());
        FmApiClientCommand command = new FmApiClientCommand(null, "tenant", "CLIENT",
                "Client", "ERP", null, List.of("org.department.read"),
                List.of(), List.of(), List.of("localhost"), 60, 1000,
                "ACTIVE", null);

        assertThrows(ServiceException.class, () -> logic.create(command));
        verifyNoInteractions(clients);
    }
}
