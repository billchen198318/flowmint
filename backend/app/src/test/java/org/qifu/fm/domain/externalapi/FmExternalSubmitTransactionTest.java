package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.external.FmExternalRequestSubmitRequest;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.entity.FmApiRequest;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.logic.IFmProcessRuntimeLogicService;
import org.qifu.fm.service.IFmApiRequestService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import tools.jackson.databind.ObjectMapper;

class FmExternalSubmitTransactionTest {
    private final IFmApiRequestService requests = mock(IFmApiRequestService.class);
    private final IFmProcessRuntimeLogicService runtime = mock(IFmProcessRuntimeLogicService.class);
    private final IFmProcessVersionService processes = mock(IFmProcessVersionService.class);
    private final IFmFormVersionService forms = mock(IFmFormVersionService.class);
    private final IFmEmployeeService employees = mock(IFmEmployeeService.class);
    private final IFmEmployeeOrgAssignmentService assignments =
            mock(IFmEmployeeOrgAssignmentService.class);
    private final RecordingTransactions transactions = new RecordingTransactions();

    @AfterEach
    void clearContext() {
        FmExternalApiContext.clear();
    }

    @Test
    void ledgerUpdateConflictRollsBackProcessSubmissionAndReservation() throws Exception {
        FmExternalRequestSubmitService service = service();
        when(requests.updateResult(any(FmApiRequest.class))).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.submit(
                OffsetDateTime.now(), request(), "same-key"));

        verify(runtime).submitAs(any(), eq("starter"));
        assertEquals(0, transactions.commits);
        assertEquals(1, transactions.rollbacks);
    }

    @Test
    void successfulLedgerUpdateCommitsWithProcessSubmission() throws Exception {
        FmExternalRequestSubmitService service = service();
        when(requests.updateResult(any(FmApiRequest.class))).thenReturn(1);

        service.submit(OffsetDateTime.now(), request(), "same-key");

        assertEquals(1, transactions.commits);
        assertEquals(0, transactions.rollbacks);
    }

    private FmExternalRequestSubmitService service() throws ServiceException {
        FmExternalApiContext.set(new FmExternalApiPrincipal("tenant", "client", "CLIENT",
                "key", Set.of("runtime.request.submit"), Set.of("process"),
                Set.of("starter"), 60, 1000, "192.0.2.1"));
        FmProcessVersion process = new FmProcessVersion();
        process.setVersionNo(1);
        FmFormVersion form = new FmFormVersion();
        form.setVersionNo(1);
        form.setContentSha256("hash");
        FmEmployee employee = new FmEmployee();
        employee.setEmployeeId("employee");
        FmEmployeeOrgAssignment assignment = new FmEmployeeOrgAssignment();
        assignment.setEmployeeOrgAssignmentId("assignment");
        assignment.setOrgUnitId("org");
        when(processes.selectListByParams(anyMap(), eq("VERSION_NO"), eq("DESC")))
                .thenReturn(result(List.of(process)));
        when(forms.selectListByParams(anyMap(), eq("VERSION_NO"), eq("DESC")))
                .thenReturn(result(List.of(form)));
        when(employees.selectListByParams(anyMap())).thenReturn(result(List.of(employee)));
        when(assignments.selectListByParams(anyMap())).thenReturn(result(List.of(assignment)));
        when(runtime.submitAs(any(), eq("starter"))).thenReturn(result(
                new FmProcessSubmitView("business", "number", "form-data",
                        "process-instance", "RUNNING")));
        return new FmExternalRequestSubmitService(requests, runtime, processes, forms,
                employees, assignments, transactions, new ObjectMapper());
    }

    private FmExternalRequestSubmitRequest request() {
        return new FmExternalRequestSubmitRequest("process", 1, "form", 1, "hash",
                Map.of(), "starter", "applicant", null, null, null);
    }

    private <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }

    private static final class RecordingTransactions extends AbstractPlatformTransactionManager {
        private int commits;
        private int rollbacks;

        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) { commits++; }
        @Override protected void doRollback(DefaultTransactionStatus status) { rollbacks++; }
    }
}
