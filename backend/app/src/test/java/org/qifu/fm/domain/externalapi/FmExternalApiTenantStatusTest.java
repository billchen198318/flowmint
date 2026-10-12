package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.tenant.FmTenantStatusGuard;
import org.qifu.fm.entity.FmApiClient;
import org.qifu.fm.entity.FmApiClientKey;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.service.IFmApiClientKeyService;
import org.qifu.fm.service.IFmApiClientService;
import org.qifu.fm.service.IFmTenantService;

import tools.jackson.databind.ObjectMapper;

class FmExternalApiTenantStatusTest {
    @Test
    void activeKeyAndClientCannotAuthenticateForDisabledTenant() throws Exception {
        IFmTenantService tenants = mock(IFmTenantService.class);
        FmTenant tenant = new FmTenant();
        tenant.setTenantId("tenant");
        tenant.setStatus("INACTIVE");
        when(tenants.selectListByParams(anyMap())).thenReturn(result(List.of(tenant)));
        FmExternalApiAuthenticator authenticator = authenticator(tenants);

        assertThrows(ServiceException.class,
                () -> authenticator.authenticate("key", "192.0.2.1"));

        tenant.setStatus("ACTIVE");
        assertEquals("tenant", authenticator.authenticate("key", "192.0.2.1").tenantId());
    }

    private FmExternalApiAuthenticator authenticator(IFmTenantService tenants)
            throws ServiceException {
        IFmApiClientKeyService keys = mock(IFmApiClientKeyService.class);
        IFmApiClientService clients = mock(IFmApiClientService.class);
        FmExternalApiKeyService keyService = mock(FmExternalApiKeyService.class);
        FmApiClientKey key = new FmApiClientKey();
        key.setKeyId("key-id");
        key.setTenantId("tenant");
        key.setClientId("client-id");
        key.setStatus("ACTIVE");
        key.setEffectiveFrom(new Date(0));
        key.setSecretHash("hash");
        FmApiClient client = new FmApiClient();
        client.setTenantId("tenant");
        client.setClientId("client-id");
        client.setStatus("ACTIVE");
        client.setRateLimitPerMinute(60);
        client.setDailyQuota(1000);
        when(keyService.extractKeyId("key")).thenReturn("key-id");
        when(keyService.matches("key", "hash")).thenReturn(true);
        when(keys.selectByKeyId("key-id")).thenReturn(key);
        when(clients.selectListByParams(anyMap())).thenReturn(result(List.of(client)));
        return new FmExternalApiAuthenticator(keys, clients, keyService,
                new FmTenantStatusGuard(tenants), new ObjectMapper());
    }

    private <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
