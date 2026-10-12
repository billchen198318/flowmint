package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmApiClientCommand;
import org.qifu.fm.logic.impl.FmExternalApiManagementLogicServiceImpl;
import org.qifu.fm.service.IFmApiClientKeyService;
import org.qifu.fm.service.IFmApiClientService;
import org.qifu.fm.service.IFmTenantService;

import tools.jackson.databind.ObjectMapper;

class FmExternalApiProcessAllowlistTest {
    @Test
    void emptyProcessListDeniesExistingClientAtRuntime() {
        FmExternalApiPrincipal principal = new FmExternalApiPrincipal("tenant", "client",
                "CLIENT", "key", Set.of("runtime.request.submit"), Set.of(), Set.of(),
                60, 1000, "192.0.2.1");
        assertFalse(principal.allowsProcess("ANY_PROCESS"));
        FmExternalApiPrincipal restricted = new FmExternalApiPrincipal("tenant", "client",
                "CLIENT", "key", Set.of("runtime.request.submit"), Set.of("ALLOWED"),
                Set.of(), 60, 1000, "192.0.2.1");
        assertTrue(restricted.allowsProcess("ALLOWED"));
        assertFalse(restricted.allowsProcess("OTHER"));
    }

    @Test
    void processScopeCannotBeSavedWithEmptyProcessList() {
        IFmApiClientService clients = mock(IFmApiClientService.class);
        FmExternalApiManagementLogicServiceImpl logic = new FmExternalApiManagementLogicServiceImpl(
                clients, mock(IFmApiClientKeyService.class),
                mock(FmExternalApiKeyService.class), mock(FmTenantAccessGuard.class),
                mock(IFmTenantService.class), new ObjectMapper());
        FmApiClientCommand command = new FmApiClientCommand(null, "tenant", "CLIENT",
                "Client", "ERP", null, List.of("runtime.request.submit"),
                List.of(), List.of(), List.of(), 60, 1000, "ACTIVE", null);

        assertThrows(ServiceException.class, () -> logic.create(command));
        verifyNoInteractions(clients);
    }
}
