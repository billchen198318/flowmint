package org.qifu.fm.domain.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.entity.FmOrgUnitVersion;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTenantAccountService;

import tools.jackson.databind.ObjectMapper;

class FmManagerSourceResolverTest {

    @Test
    void resolvesOwnAndParentFormalHeadsThroughActiveAssignments() throws Exception {
        IFmEmployeeService employees = mock(IFmEmployeeService.class);
        IFmEmployeeOrgAssignmentService assignments = mock(IFmEmployeeOrgAssignmentService.class);
        IFmOrgUnitHeadService heads = mock(IFmOrgUnitHeadService.class);
        IFmOrgUnitVersionService units = mock(IFmOrgUnitVersionService.class);
        IFmTenantAccountService memberships = mock(IFmTenantAccountService.class);
        FmAssignmentResolverService resolver = new FmAssignmentResolverService(
                employees, assignments, heads, units, null, null, null, null,
                null, null, null, null, null, memberships, new ObjectMapper());
        FmEmployeeOrgAssignment ownManager = assignment("own-manager", "own", "e1");
        FmEmployeeOrgAssignment parentManager = assignment("parent-manager", "parent", "e2");
        when(assignments.selectListByParams(anyMap(), anyString(), anyString()))
                .thenAnswer(call -> {
                    Map<?, ?> params = call.getArgument(0);
                    return result("e1".equals(params.get("employeeId"))
                            ? List.of(ownManager) : List.of(parentManager));
                });
        when(heads.selectListByParams(anyMap(), anyString(), anyString()))
                .thenAnswer(call -> {
                    Map<?, ?> params = call.getArgument(0);
                    String unit = (String) params.get("orgUnitId");
                    FmOrgUnitHead head = new FmOrgUnitHead();
                    head.setHeadType("HEAD");
                    head.setEmployeeId("own".equals(unit) ? "e1" : "e2");
                    return result(List.of(head));
                });
        when(employees.selectListByParams(anyMap())).thenAnswer(call -> {
            Map<?, ?> params = call.getArgument(0);
            FmEmployee employee = new FmEmployee();
            employee.setEmployeeId((String) params.get("employeeId"));
            employee.setAccount("manager");
            return result(List.of(employee));
        });
        when(memberships.selectListByParams(anyMap()))
                .thenReturn(result(List.of(new FmTenantAccount())));
        FmOrgUnitVersion ownUnit = new FmOrgUnitVersion();
        ownUnit.setParentOrgUnitId("parent");
        when(units.selectListByParams(anyMap(), anyString(), anyString()))
                .thenReturn(result(List.of(ownUnit)));

        FmEmployeeOrgAssignment applicant = assignment("applicant", "own", "e0");
        applicant.setManagerSource("ORG_HEAD");
        assertEquals("own-manager", resolver.managerAssignment("tenant", applicant)
                .getEmployeeOrgAssignmentId());
        applicant.setManagerSource("PARENT_HEAD");
        assertEquals("parent-manager", resolver.managerAssignment("tenant", applicant)
                .getEmployeeOrgAssignmentId());
        applicant.setManagerSource("NONE");
        assertNull(resolver.managerAssignment("tenant", applicant));
    }

    private static FmEmployeeOrgAssignment assignment(String id, String unit, String employeeId) {
        FmEmployeeOrgAssignment value = new FmEmployeeOrgAssignment();
        value.setEmployeeOrgAssignmentId(id);
        value.setOrgUnitId(unit);
        value.setEmployeeId(employeeId);
        return value;
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
