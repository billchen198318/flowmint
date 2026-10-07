package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.entity.TbAccount;
import org.qifu.core.service.IAccountService;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmEmployeeCommand;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmTenantService;

class FmEmployeeTenantAccessTest {

    @Test
    void employeeCreateRejectsInaccessibleTenantBeforeInsert() throws Exception {
        IFmEmployeeService employees = mock(IFmEmployeeService.class);
        FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
        doThrow(new ServiceException("DENIED")).when(access).requireAccess("T-OTHER");
        @SuppressWarnings("unchecked")
        IAccountService<TbAccount, String> accounts = mock(IAccountService.class);
        FmEmployeeLogicServiceImpl logic = new FmEmployeeLogicServiceImpl(employees,
                mock(IFmTenantService.class), mock(IFmTenantAccountService.class),
                accounts, access);
        FmEmployeeCommand command = mock(FmEmployeeCommand.class);
        when(command.tenantId()).thenReturn("T-OTHER");

        assertThrows(ServiceException.class, () -> logic.create(command));
        verifyNoInteractions(employees);
    }

    @Test
    void assignmentListRejectsInaccessibleEmployeeTenant() throws Exception {
        IFmEmployeeService employees = mock(IFmEmployeeService.class);
        IFmEmployeeOrgAssignmentService assignments = mock(IFmEmployeeOrgAssignmentService.class);
        FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
        FmEmployee employee = new FmEmployee();
        employee.setTenantId("T-OTHER");
        DefaultResult<FmEmployee> result = new DefaultResult<>();
        result.setValue(employee);
        when(employees.selectByPrimaryKey("E-1")).thenReturn(result);
        doThrow(new ServiceException("DENIED")).when(access).requireAccess("T-OTHER");
        FmEmployeeOrgAssignmentLogicServiceImpl logic = new FmEmployeeOrgAssignmentLogicServiceImpl(
                employees, assignments, mock(IFmOrgUnitVersionService.class),
                mock(IFmOrgTitleService.class), access);

        assertThrows(ServiceException.class, () -> logic.list("E-1"));
        verifyNoInteractions(assignments);
    }
}
