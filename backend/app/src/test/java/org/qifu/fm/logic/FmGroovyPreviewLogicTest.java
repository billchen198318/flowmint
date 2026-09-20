package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.workflow.FmGroovyDesignAccess;
import org.qifu.fm.domain.workflow.FmGroovyPreviewQuota;
import org.qifu.fm.domain.workflow.FmGroovyPreviewRunner;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.dto.command.FmGroovyPreviewCommand;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.logic.impl.FmGroovyPreviewLogicServiceImpl;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessVersionService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

class FmGroovyPreviewLogicTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String XML = """
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:fm="https://flowmint.qifu.org/schema/bpmn" targetNamespace="test">
                <process id="P1" isExecutable="true">
                    <startEvent id="start" />
                    <serviceTask id="calculate" fm:taskType="GROOVY" fm:bindingId="calculation" />
                    <endEvent id="end" />
                    <sequenceFlow id="f1" sourceRef="start" targetRef="calculate" />
                    <sequenceFlow id="f2" sourceRef="calculate" targetRef="end" />
                </process>
            </definitions>
            """;
    private static final String SCHEMA = """
            {"type":"object","required":["amount"],"properties":{"amount":{"type":"number"}}}
            """;
    private final IFmProcessVersionService versions = mock(IFmProcessVersionService.class);
    private final IFmProcessDefService definitions = mock(IFmProcessDefService.class);
    private final FmGroovyDesignAccess access = mock(FmGroovyDesignAccess.class);
    private final FmGroovyPreviewRunner runner = mock(FmGroovyPreviewRunner.class);
    private final FmGroovyPreviewQuota quota = new FmGroovyPreviewQuota();
    private final AtomicReference<JsonNode> captured = new AtomicReference<>();
    private FmProcessVersion version;

    @BeforeEach
    void setup() throws Exception {
        version = new FmProcessVersion();
        version.setOid("V1");
        version.setTenantId("T1");
        version.setProcessDefId("D1");
        version.setVersionNo(1);
        version.setVersionStatus("DRAFT");
        version.setBpmnXml("stored XML remains unchanged");
        when(versions.selectByPrimaryKey("V1")).thenReturn(result(version));
        when(versions.findDraftLockVersion("T1", "V1")).thenReturn(2);
        FmProcessDef definition = new FmProcessDef();
        definition.setProcessKey("P1");
        when(definitions.selectListByParams(any())).thenReturn(result(List.of(definition)));
        when(runner.profile()).thenReturn(new FmGroovyVersionManifest.EngineProfile(
                "5.0.6", "ExampleJDK", "21.0.10+7", "local/worker@sha256:" + "a".repeat(64), "1"));
        when(runner.execute(any(), anyInt())).thenAnswer(call -> {
            JsonNode request = JSON.readTree((byte[]) call.getArgument(0));
            captured.set(request);
            ObjectNode response = JSON.createObjectNode();
            for (String field : List.of("protocolVersion", "profile", "invocationId", "attemptId", "generation", "bindingSha256")) {
                response.set(field, request.get(field));
            }
            if ("CHECK".equals(request.get("operation").asText())) {
                response.put("status", "VALID");
            } else {
                response.put("status", "SUCCEEDED");
                response.putObject("result").put("amount", 25);
                response.putArray("logs");
                response.put("logsTruncated", false);
            }
            return JSON.writeValueAsBytes(response);
        });
    }

    @Test
    void checkRequiresNoSampleAndBuildsTrustedContextWithoutMutatingVersion() throws Exception {
        var view = logic(true).check(command(null)).getValue();
        assertEquals("VALID", view.status());
        assertEquals("PREVIEW", captured.get().path("context").path("mode").asText());
        assertEquals("T1", captured.get().path("context").path("tenantId").asText());
        assertTrue(captured.get().path("context").path("applicantAccount").isNull());
        assertTrue(captured.get().path("input").isEmpty());
        assertEquals("stored XML remains unchanged", version.getBpmnXml());
        assertNotNull(view.manifestSha256());
        verify(versions, never()).update(any());
        verify(versions, never()).lockDraft(any(), any());
    }

    @Test
    void runUsesOnlyManualInputAndReturnsValidatedOutput() throws Exception {
        var view = logic(true).preview(command("{\"amount\":12.5}")).getValue();
        assertEquals("SUCCEEDED", view.status());
        assertEquals(25, view.result().path("amount").asInt());
        assertEquals(12.5, captured.get().path("input").path("amount").asDouble());
        assertEquals(2, view.expectedLockVersion());
        verify(runner).execute(any(), org.mockito.ArgumentMatchers.eq(3000));
        verify(versions, never()).update(any());
    }

    @Test
    void rejectsDisabledUnauthorizedStaleAndPublishedBeforeRunnerExecution() throws Exception {
        assertThrows(ServiceException.class, () -> logic(false).check(command(null)));
        verifyNoInteractions(versions, access, runner);
        when(versions.findDraftLockVersion("T1", "V1")).thenReturn(3);
        assertThrows(ServiceException.class, () -> logic(true).check(command(null)));
        when(versions.findDraftLockVersion("T1", "V1")).thenReturn(2);
        version.setVersionStatus("PUBLISHED");
        assertThrows(ServiceException.class, () -> logic(true).check(command(null)));
        version.setVersionStatus("DRAFT");
        doThrow(new ServiceException("DENIED")).when(access).require("T1");
        assertThrows(ServiceException.class, () -> logic(true).check(command(null)));
        verify(runner, never()).execute(any(), anyInt());
    }

    @Test
    void ignoresCheckSampleButRejectsInvalidRunInputBeforeExecuting() throws Exception {
        for (String sample : List.of("{}", "{\"amount\":\"12\"}", "{\"amount\":1,\"tenantId\":\"other\"}", "{} {}")) {
            assertThrows(ServiceException.class, () -> logic(true).preview(command(sample)));
        }
        verify(runner, never()).execute(any(), anyInt());
        assertEquals("VALID", logic(true).check(command("invalid ignored sample")).getValue().status());
    }

    @Test
    void discardsResponseWhenDraftChangesDuringExecutionAndReleasesQuota() throws Exception {
        when(versions.findDraftLockVersion("T1", "V1")).thenReturn(2, 2, 3);
        assertEquals("GROOVY_PREVIEW_STALE_VERSION",
                assertThrows(ServiceException.class, () -> logic(true).check(command(null))).getMessage());
        try (var first = quota.acquire("T1"); var second = quota.acquire("T1")) {
            assertNotNull(first);
            assertNotNull(second);
        }
        verify(versions, never()).update(any());
    }

    @Test
    void invalidWorkerResponseFailsClosedAndDoesNotLeakPayload() throws Exception {
        doReturn("{\"secret\":\"sample-value\"}".getBytes()).when(runner).execute(any(), anyInt());
        assertEquals("GROOVY_PREVIEW_CONTRACT_INVALID",
                assertThrows(ServiceException.class, () -> logic(true).preview(command("{\"amount\":1}"))).getMessage());
    }

    @Test
    void concurrentPublicationBetweenEntityAndLockReadIsRejected() throws Exception {
        when(versions.findDraftLockVersion("T1", "V1")).thenReturn(null);
        assertEquals("GROOVY_PREVIEW_STALE_VERSION",
                assertThrows(ServiceException.class, () -> logic(true).check(command(null))).getMessage());
        verify(runner, never()).execute(any(), anyInt());
    }

    private FmGroovyPreviewLogicServiceImpl logic(boolean enabled) {
        return new FmGroovyPreviewLogicServiceImpl(versions, definitions, access, runner, quota, enabled);
    }

    private FmGroovyPreviewCommand command(String sample) {
        var binding = new FmGroovyBindingCommand("calculate", "calculation", "return [amount: input.amount * 2]\n",
                SCHEMA, SCHEMA, """
                {"mappingVersion":1,"input":{"amount":{"source":"FORM_DATA","path":"amount"}},
                 "output":{"amount":{"target":"FORM_DATA","path":"total"}}}
                """, 3000);
        return new FmGroovyPreviewCommand("V1", 2, XML, List.of(binding), "calculate", sample);
    }

    private <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
