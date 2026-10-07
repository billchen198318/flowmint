package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmEmployeeOrgAssignmentCommand;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.qifu.fm.service.IFmOrgUnitVersionService;

class FmEmployeeOrgAssignmentRulesTest {

    @Test
    void rejectsFutureExplicitManagerAssignment() throws Exception {
        Fixture fixture = new Fixture();
        FmEmployeeOrgAssignment manager = fixture.assignment("MANAGER", "N",
                new Date(System.currentTimeMillis() + 86_400_000L), null);
        manager.setEmployeeOrgAssignmentId("MANAGER-ID");
        manager.setEmployeeId("OTHER");
        fixture.assignments(List.of(manager));

        assertThrows(ServiceException.class, () -> fixture.logic.save(
                fixture.command("EXPLICIT", "MANAGER-ID", "N",
                        new Date(), null)));
    }

    @Test
    void allowsNextPrimaryAssignmentAfterPreviousPeriodEnds() throws Exception {
        Fixture fixture = new Fixture();
        Date start = new Date(System.currentTimeMillis() + 86_400_000L);
        FmEmployeeOrgAssignment previous = fixture.assignment("OLD", "Y",
                new Date(System.currentTimeMillis() - 86_400_000L), start);
        fixture.assignments(List.of(previous));

        assertDoesNotThrow(() -> fixture.logic.save(
                fixture.command("NONE", null, "Y", start, null)));
        verify(fixture.assignmentService).insert(any(FmEmployeeOrgAssignment.class));
    }

    @Test
    void deactivatingFutureAssignmentKeepsValidDateRange() throws Exception {
        Fixture fixture = new Fixture();
        FmEmployeeOrgAssignment future = fixture.assignment("FUTURE", "N",
                new Date(System.currentTimeMillis() + 86_400_000L), null);
        future.setTenantId("T-1");
        future.setEmployeeId("EMP-1");
        DefaultResult<FmEmployeeOrgAssignment> result = new DefaultResult<>();
        result.setValue(future);
        when(fixture.assignmentService.selectByPrimaryKey("FUTURE")).thenReturn(result);
        fixture.assignments(List.of(future));

        assertDoesNotThrow(() -> fixture.logic.deactivate("E-OID", "FUTURE"));
        assertEquals("INACTIVE", future.getStatus());
        assertNull(future.getEffectiveTo());
        verify(fixture.assignmentService).update(future);
    }

    private static final class Fixture {
        final IFmEmployeeService employeeService = mock(IFmEmployeeService.class);
        final IFmEmployeeOrgAssignmentService assignmentService = mock(IFmEmployeeOrgAssignmentService.class);
        final IFmOrgUnitVersionService unitService = mock(IFmOrgUnitVersionService.class);
        final IFmOrgTitleService titleService = mock(IFmOrgTitleService.class);
        final FmEmployeeOrgAssignmentLogicServiceImpl logic;

        Fixture() throws Exception {
            FmEmployee employee = new FmEmployee();
            employee.setOid("E-OID");
            employee.setTenantId("T-1");
            employee.setEmployeeId("EMP-1");
            DefaultResult<FmEmployee> employeeResult = new DefaultResult<>();
            employeeResult.setValue(employee);
            when(employeeService.selectByPrimaryKey("E-OID")).thenReturn(employeeResult);
            DefaultResult<List<FmEmployee>> employees = new DefaultResult<>();
            employees.setValue(List.of());
            when(employeeService.selectListByParams(anyMap(), anyString(), anyString()))
                    .thenReturn(employees);
            FmOrgUnitView unit = new FmOrgUnitView();
            unit.setOrgUnitId("U-1");
            unit.setUnitCode("U");
            unit.setUnitName("Unit");
            DefaultResult<List<FmOrgUnitView>> units = new DefaultResult<>();
            units.setValue(List.of(unit));
            when(unitService.selectCurrentTree(anyMap())).thenReturn(units);
            FmOrgTitle title = new FmOrgTitle();
            title.setTitleId("TITLE-1");
            title.setTitleCode("TITLE");
            title.setTitleName("Title");
            DefaultResult<List<FmOrgTitle>> titles = new DefaultResult<>();
            titles.setValue(List.of(title));
            when(titleService.selectListByParams(anyMap(), anyString(), anyString()))
                    .thenReturn(titles);
            logic = new FmEmployeeOrgAssignmentLogicServiceImpl(employeeService,
                    assignmentService, unitService, titleService,
                    mock(FmTenantAccessGuard.class));
        }

        void assignments(List<FmEmployeeOrgAssignment> values) throws Exception {
            DefaultResult<List<FmEmployeeOrgAssignment>> result = new DefaultResult<>();
            result.setValue(values);
            when(assignmentService.selectListByParams(anyMap(), anyString(), anyString()))
                    .thenReturn(result);
        }

        FmEmployeeOrgAssignment assignment(String oid, String primary, Date from, Date to) {
            FmEmployeeOrgAssignment value = new FmEmployeeOrgAssignment();
            value.setOid(oid);
            value.setIsPrimary(primary);
            value.setStatus("ACTIVE");
            value.setEffectiveFrom(from);
            value.setEffectiveTo(to);
            return value;
        }

        FmEmployeeOrgAssignmentCommand command(String source, String managerId,
                String primary, Date from, Date to) {
            return new FmEmployeeOrgAssignmentCommand(null, "E-OID", "U-1", "TITLE-1",
                    source, managerId, primary, "ACTIVE", from, to);
        }
    }
}
