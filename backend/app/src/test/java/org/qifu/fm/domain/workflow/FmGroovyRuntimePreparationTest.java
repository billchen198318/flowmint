package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessVersion;

import tools.jackson.databind.json.JsonMapper;

class FmGroovyRuntimePreparationTest {

    private static final String XML = """
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:fm="https://flowmint.qifu.org/schema/bpmn" targetNamespace="test">
                <process id="P" isExecutable="true">
                    <startEvent id="start" />
                    <serviceTask id="calculate" fm:taskType="GROOVY" fm:bindingId="calculation" />
                    <endEvent id="end" />
                    <sequenceFlow id="f1" sourceRef="start" targetRef="calculate" />
                    <sequenceFlow id="f2" sourceRef="calculate" targetRef="end" />
                </process>
            </definitions>
            """;
    private static final String FORM = """
            {"components":[
              {"type":"number","key":"amount","validate":{"required":true}},
              {"type":"number","key":"total","properties":{"flowmintSystemTaskWritable":true}}
            ]}
            """;
    private static final List<FmGroovyBindingCommand> BINDINGS = List.of(new FmGroovyBindingCommand(
            "calculate", "calculation", "return [total: input.amount * 2G]",
            """
            {"type":"object","required":["amount"],"properties":{"amount":{"type":"number"}}}
            """,
            """
            {"type":"object","properties":{"total":{"type":"number"}}}
            """,
            """
            {"mappingVersion":1,"input":{"amount":{"source":"FORM_DATA","path":"amount"}},
             "output":{"total":{"target":"FORM_DATA","path":"total"}}}
            """, 3000));
    private static final FmGroovyVersionManifest.EngineProfile PROFILE =
            new FmGroovyVersionManifest.EngineProfile("5.0.6", "ExampleJDK", "21.0.10+7",
                    "local/worker@sha256:" + "a".repeat(64), "1");

    @Test
    void preparesInputAndWritePlanFromVerifiedPublishedOrRetiredContent() throws Exception {
        var version = version();
        var manifest = FmGroovyVersionManifest.build(version, "P", BINDINGS, PROFILE);
        for (String status : List.of("PUBLISHED", "RETIRED")) {
            version.setVersionStatus(status);
            var preparation = prepare(version, manifest, "calculate", FORM);
            var input = preparation.input("{\"amount\":12.5}", context("T"), 3);
            assertEquals("{\"amount\":12.5}", input.inputJson());
            assertEquals(3, input.revisionNo());
            assertEquals(manifest.sha256(), preparation.manifestSha256());
            assertEquals(manifest.bindings().getFirst().sha256(), preparation.bindingSha256());
            var result = preparation.apply("{\"amount\":12.5}", "{\"total\":25}",
                    new FmFormSubmissionValidator(JsonMapper.builder().build()));
            assertTrue(result.requiresWrite());
            assertEquals("{\"amount\":12.5,\"total\":25}", result.formJson());
        }
    }

    @Test
    void rejectsDraftChangedContentUnknownNodeAndUnwritableForm() throws Exception {
        var version = version();
        var manifest = FmGroovyVersionManifest.build(version, "P", BINDINGS, PROFILE);
        version.setVersionStatus("DRAFT");
        assertThrows(IllegalArgumentException.class, () -> prepare(version, manifest, "calculate", FORM));
        version.setVersionStatus("PUBLISHED");
        assertThrows(IllegalArgumentException.class, () -> prepare(version, manifest, "missing", FORM));
        assertThrows(IllegalArgumentException.class, () -> prepare(version, manifest, "calculate",
                FORM.replace("flowmintSystemTaskWritable", "other")));
        version.setBpmnXml(XML + " ");
        assertThrows(IllegalArgumentException.class, () -> prepare(version, manifest, "calculate", FORM));
    }

    @Test
    void rejectsForeignContextAndInvalidOutputWithoutMutatingLoadedVersion() throws Exception {
        var version = version();
        var manifest = FmGroovyVersionManifest.build(version, "P", BINDINGS, PROFILE);
        var preparation = prepare(version, manifest, "calculate", FORM);
        assertThrows(IllegalArgumentException.class,
                () -> preparation.input("{\"amount\":1}", context("OTHER"), 1));
        assertThrows(IllegalArgumentException.class,
                () -> preparation.input("{}", context("T"), 1));
        assertThrows(IllegalArgumentException.class, () -> preparation.apply("{\"amount\":1}",
                "{\"total\":2,\"tenantId\":\"OTHER\"}",
                new FmFormSubmissionValidator(JsonMapper.builder().build())));
        assertEquals(XML, version.getBpmnXml());
        assertEquals("PUBLISHED", version.getVersionStatus());
    }

    private FmGroovyRuntimePreparation prepare(FmProcessVersion version,
            FmGroovyVersionManifest.Manifest manifest, String node, String form) throws Exception {
        return new FmGroovyRuntimePreparation(version, "P", BINDINGS, PROFILE,
                manifest.canonicalJson(), manifest.sha256(), node, form);
    }

    private FmProcessVersion version() {
        var version = new FmProcessVersion();
        version.setTenantId("T");
        version.setProcessDefId("P");
        version.setVersionNo(1);
        version.setVersionStatus("PUBLISHED");
        version.setBpmnXml(XML);
        version.setBpmnSha256(FmGroovyContractJson.sha256(XML));
        return version;
    }

    private FmGroovyRuntimeMapping.TrustedContext context(String tenant) {
        return new FmGroovyRuntimeMapping.TrustedContext(tenant, "P", 1, "instance", "calculate", "invocation",
                Instant.parse("2026-09-10T00:00:00Z"), "business", null, "applicant", "initiator", null);
    }
}
