package org.qifu.fm.logic.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.entity.TbAccount;
import org.qifu.core.entity.TbUserRole;
import org.qifu.core.service.IAccountService;
import org.qifu.core.service.IUserRoleService;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.dto.command.FmTenantAccountCommand;
import org.qifu.fm.dto.command.FmResetPasswordCommand;
import org.qifu.fm.dto.command.FmTenantCommand;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.tenant.FmAccountTokenRevoker;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.security.crypto.password.PasswordEncoder;

class FmTenantAccountActivationTest {

    @Test
    void membershipUpdateRejectsReversedEffectivePeriod() throws Exception {
        IFmTenantService tenants = mock(IFmTenantService.class);
        IFmTenantAccountService memberships = mock(IFmTenantAccountService.class);
        @SuppressWarnings("unchecked")
        IAccountService<TbAccount, String> accounts = mock(IAccountService.class);
        @SuppressWarnings("unchecked")
        IUserRoleService<TbUserRole, String> roles = mock(IUserRoleService.class);
        FmTenant tenant = new FmTenant();
        tenant.setOid("T-OID");
        tenant.setTenantId("T-1");
        DefaultResult<FmTenant> tenantResult = new DefaultResult<>();
        tenantResult.setValue(tenant);
        when(tenants.selectByPrimaryKey("T-OID")).thenReturn(tenantResult);
        FmTenantLogicServiceImpl logic = new FmTenantLogicServiceImpl(tenants,
                memberships, accounts, roles, mock(PasswordEncoder.class),
                mock(FmAccountTokenRevoker.class));

        Date start = new Date(System.currentTimeMillis() + 60_000);
        Date end = new Date();
        assertThrows(ServiceException.class, () -> logic.updateAccount(
                new FmTenantAccountCommand("M-1", "T-OID", "user", false,
                        null, null, "N", "ACTIVE", start, end)));
        verify(memberships, never()).update(any(FmTenantAccount.class));
    }

    @Test
    void invalidTenantTimezoneIsRejectedBeforeSaving() {
        IFmTenantService tenants = mock(IFmTenantService.class);
        @SuppressWarnings("unchecked")
        IAccountService<TbAccount, String> accounts = mock(IAccountService.class);
        @SuppressWarnings("unchecked")
        IUserRoleService<TbUserRole, String> roles = mock(IUserRoleService.class);
        FmTenantLogicServiceImpl logic = new FmTenantLogicServiceImpl(tenants,
                mock(IFmTenantAccountService.class), accounts, roles,
                mock(PasswordEncoder.class), mock(FmAccountTokenRevoker.class));

        assertThrows(ServiceException.class, () -> logic.create(new FmTenantCommand(
                null, "T-1", "TENANT", "Tenant", "zh-TW", "bad/timezone",
                "ACTIVE", null)));
        verify(tenants, never()).insert(any(FmTenant.class));
    }

    @Test
    void passwordResetRevokesExistingTokens() throws Exception {
        IFmTenantService tenants = mock(IFmTenantService.class);
        IFmTenantAccountService memberships = mock(IFmTenantAccountService.class);
        @SuppressWarnings("unchecked")
        IAccountService<TbAccount, String> accounts = mock(IAccountService.class);
        @SuppressWarnings("unchecked")
        IUserRoleService<TbUserRole, String> roles = mock(IUserRoleService.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        FmAccountTokenRevoker revoker = mock(FmAccountTokenRevoker.class);
        FmTenant tenant = new FmTenant();
        tenant.setOid("T-OID");
        tenant.setTenantId("T-1");
        FmTenantAccount membership = new FmTenantAccount();
        membership.setTenantId("T-1");
        membership.setAccount("user");
        TbAccount account = new TbAccount();
        account.setAccount("user");
        DefaultResult<FmTenant> tenantResult = new DefaultResult<>();
        tenantResult.setValue(tenant);
        DefaultResult<List<FmTenantAccount>> membershipResult = new DefaultResult<>();
        membershipResult.setValue(List.of(membership));
        DefaultResult<TbAccount> accountResult = new DefaultResult<>();
        accountResult.setValue(account);
        when(tenants.selectByPrimaryKey("T-OID")).thenReturn(tenantResult);
        when(memberships.selectListByParams(anyMap(), anyString(), anyString()))
                .thenReturn(membershipResult);
        when(accounts.selectByUniqueKey(any(TbAccount.class))).thenReturn(accountResult);
        when(encoder.encode("new-password")).thenReturn("encoded");

        FmTenantLogicServiceImpl logic = new FmTenantLogicServiceImpl(tenants, memberships,
                accounts, roles, encoder, revoker);
        logic.resetPassword(new FmResetPasswordCommand("T-OID", "user",
                "new-password", "new-password"));

        verify(accounts).update(account);
        verify(revoker).revokeAll("user");
    }

    @Test
    void changingDefaultTenantClearsThePreviousDefault() throws Exception {
        IFmTenantService tenants = mock(IFmTenantService.class);
        IFmTenantAccountService memberships = mock(IFmTenantAccountService.class);
        @SuppressWarnings("unchecked")
        IAccountService<TbAccount, String> accounts = mock(IAccountService.class);
        @SuppressWarnings("unchecked")
        IUserRoleService<TbUserRole, String> roles = mock(IUserRoleService.class);
        FmTenant tenant = new FmTenant();
        tenant.setOid("T-2-OID");
        tenant.setTenantId("T-2");
        FmTenantAccount previous = new FmTenantAccount();
        previous.setOid("M-1");
        previous.setTenantId("T-1");
        previous.setAccount("user");
        previous.setIsDefault("Y");
        FmTenantAccount selected = new FmTenantAccount();
        selected.setOid("M-2");
        selected.setTenantId("T-2");
        selected.setAccount("user");
        selected.setIsDefault("N");
        DefaultResult<FmTenant> tenantResult = new DefaultResult<>();
        tenantResult.setValue(tenant);
        DefaultResult<List<FmTenantAccount>> membershipResult = new DefaultResult<>();
        membershipResult.setValue(List.of(previous, selected));
        DefaultResult<FmTenantAccount> selectedResult = new DefaultResult<>();
        selectedResult.setValue(selected);
        when(tenants.selectByPrimaryKey("T-2-OID")).thenReturn(tenantResult);
        when(memberships.selectByPrimaryKey("M-2")).thenReturn(selectedResult);
        when(memberships.selectListByParams(anyMap())).thenReturn(membershipResult);
        when(memberships.selectListByParams(anyMap(), anyString(), anyString()))
                .thenReturn(membershipResult);

        FmTenantLogicServiceImpl logic = new FmTenantLogicServiceImpl(tenants, memberships,
                accounts, roles, mock(PasswordEncoder.class), mock(FmAccountTokenRevoker.class));
        logic.updateAccount(new FmTenantAccountCommand("M-2", "T-2-OID", "user", false,
                null, null, "Y", "ACTIVE", new Date(), null));

        verify(memberships).update(previous);
        verify(memberships).update(selected);
        org.junit.jupiter.api.Assertions.assertEquals("N", previous.getIsDefault());
        org.junit.jupiter.api.Assertions.assertEquals("Y", selected.getIsDefault());
    }

    @Test
    void globalActivationPreservesTenantMembership() throws Exception {
        IFmTenantService tenants = mock(IFmTenantService.class);
        IFmTenantAccountService memberships = mock(IFmTenantAccountService.class);
        @SuppressWarnings("unchecked")
        IAccountService<TbAccount, String> accounts = mock(IAccountService.class);
        @SuppressWarnings("unchecked")
        IUserRoleService<TbUserRole, String> roles = mock(IUserRoleService.class);
        FmTenant tenant = new FmTenant();
        tenant.setOid("T-OID");
        tenant.setTenantId("T-1");
        FmTenantAccount membership = new FmTenantAccount();
        membership.setTenantId("T-1");
        membership.setAccount("user");
        membership.setStatus("INACTIVE");
        TbAccount account = new TbAccount();
        account.setAccount("user");
        account.setOnJob("N");
        DefaultResult<FmTenant> tenantResult = new DefaultResult<>();
        tenantResult.setValue(tenant);
        DefaultResult<List<FmTenantAccount>> membershipResult = new DefaultResult<>();
        membershipResult.setValue(List.of(membership));
        DefaultResult<TbAccount> accountResult = new DefaultResult<>();
        accountResult.setValue(account);
        when(tenants.selectByPrimaryKey("T-OID")).thenReturn(tenantResult);
        when(memberships.selectListByParams(anyMap(), anyString(), anyString()))
                .thenReturn(membershipResult);
        when(accounts.selectByUniqueKey(any(TbAccount.class))).thenReturn(accountResult);

        FmTenantLogicServiceImpl logic = new FmTenantLogicServiceImpl(tenants, memberships,
                accounts, roles, mock(PasswordEncoder.class), mock(FmAccountTokenRevoker.class));
        logic.activateAccount("T-OID", "user");

        verify(accounts).update(account);
        verify(memberships, never()).update(any(FmTenantAccount.class));
    }
}
