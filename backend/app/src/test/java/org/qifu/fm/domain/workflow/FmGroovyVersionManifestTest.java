package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessVersion;

class FmGroovyVersionManifestTest {

    private static final String XML = """
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:fm="https://flowmint.qifu.org/schema/bpmn" targetNamespace="test">
                <process id="P1" isExecutable="true">
                    <startEvent id="start" />
                    <serviceTask id="calculate" fm:taskType="GROOVY" fm:bindingId="calculation" />
                    <serviceTask id="convert" fm:taskType="GROOVY" fm:bindingId="conversion" />
                    <endEvent id="end" />
                    <sequenceFlow id="f1" sourceRef="start" targetRef="calculate" />
                    <sequenceFlow id="f2" sourceRef="calculate" targetRef="convert" />
                    <sequenceFlow id="f3" sourceRef="convert" targetRef="end" />
                </process>
            </definitions>
            """;
    private static final String SCHEMA = """
            {"type":"object","properties":{"amount":{"type":"number"}}}
            """;
    private static final String MAPPING = """
            {"mappingVersion":1,"input":{"amount":{"source":"CONSTANT","value":1}},
             "output":{"amount":{"target":"FORM_DATA","path":"calculatedAmount"}}}
            """;

    @Test
    void sortsBindingsAndDoesNotExposeScriptsInManifest() throws Exception {
        var first = binding("calculate", "calculation", "return [amount: input.amount]\n");
        var second = binding("convert", "conversion", "return [amount: 2]\n");
        var manifest = FmGroovyVersionManifest.build(version(), "P1", List.of(second, first), profile("a"));
        assertEquals(manifest, FmGroovyVersionManifest.build(version(), "P1", List.of(first, second), profile("a")));
        assertEquals("calculation", manifest.bindings().getFirst().bindingId());
        assertEquals(FmGroovyContractJson.sha256(XML), manifest.bpmnSha256());
        assertFalse(manifest.canonicalJson().contains("return"));
        assertThrows(UnsupportedOperationException.class, () -> manifest.bindings().clear());
    }

    @Test
    void normalizationKeepsEquivalentContractsStable() throws Exception {
        var original = commands();
        var equivalent = new FmGroovyBindingCommand("calculate", "calculation",
                "\uFEFFreturn [amount: input.amount]\r\n",
                "{\"properties\":{\"amount\":{\"type\":\"number\"}},\"type\":\"object\"}",
                SCHEMA, MAPPING.replace("\"value\":1", "\"value\":1.0"), 3000);
        assertEquals(FmGroovyVersionManifest.build(version(), "P1", original, profile("a")),
                FmGroovyVersionManifest.build(version(), "P1", List.of(equivalent, original.get(1)), profile("a")));
    }

    @Test
    void scriptContractTimeoutAndImageChangesInvalidateIdentity() throws Exception {
        var original = commands();
        var first = original.getFirst();
        var baseline = FmGroovyVersionManifest.build(version(), "P1", original, profile("a"));
        for (FmGroovyBindingCommand changed : List.of(
                binding("calculate", "calculation", "return [amount: 99]\n"),
                new FmGroovyBindingCommand(first.nodeId(), first.bindingId(), first.scriptContent(),
                        SCHEMA, SCHEMA, MAPPING.replace("calculatedAmount", "otherAmount"), 3000),
                new FmGroovyBindingCommand(first.nodeId(), first.bindingId(), first.scriptContent(),
                        SCHEMA, SCHEMA, MAPPING, 4000),
                new FmGroovyBindingCommand(first.nodeId(), first.bindingId(), first.scriptContent(),
                        SCHEMA, SCHEMA.replace("\"number\"", "\"integer\""), MAPPING, 3000))) {
            var result = FmGroovyVersionManifest.build(version(), "P1", List.of(changed, original.get(1)), profile("a"));
            assertNotEquals(baseline.sha256(), result.sha256());
            assertNotEquals(baseline.bindings().getFirst().sha256(), result.bindings().getFirst().sha256());
        }
        var otherImage = FmGroovyVersionManifest.build(version(), "P1", original, profile("b"));
        assertNotEquals(baseline.sha256(), otherImage.sha256());
        assertNotEquals(baseline.bindings().getFirst().sha256(), otherImage.bindings().getFirst().sha256());
        var otherJdk = new FmGroovyVersionManifest.EngineProfile("5.0.6", "ExampleJDK", "21.0.11+1",
                profile("a").workerImage(), "1");
        assertNotEquals(baseline.sha256(), FmGroovyVersionManifest.build(version(), "P1", original, otherJdk).sha256());
    }

    @Test
    void xmlTenantAndVersionAreBoundToManifest() throws Exception {
        var baseline = FmGroovyVersionManifest.build(version(), "P1", commands(), profile("a"));
        FmProcessVersion changed = version();
        changed.setTenantId("T2");
        assertNotEquals(baseline.sha256(), FmGroovyVersionManifest.build(changed, "P1", commands(), profile("a")).sha256());
        changed = version();
        changed.setVersionNo(2);
        assertNotEquals(baseline.sha256(), FmGroovyVersionManifest.build(changed, "P1", commands(), profile("a")).sha256());
        changed = version();
        changed.setBpmnXml(XML.replace("id=\"end\"", "id=\"end\" name=\"完成\""));
        assertNotEquals(baseline.sha256(), FmGroovyVersionManifest.build(changed, "P1", commands(), profile("a")).sha256());
    }

    @Test
    void rejectsUnpinnedProfileAndMismatchedBindingsWithoutEnablingPublication() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyVersionManifest.EngineProfile(
                "5.0.6", "ExampleJDK", "21.0.10+7", "worker:latest", "1"));
        for (String incompleteVersion : List.of("21", "21.0", "21.0.10")) {
            assertThrows(IllegalArgumentException.class, () -> new FmGroovyVersionManifest.EngineProfile(
                    "5.0.6", "ExampleJDK", incompleteVersion, profile("a").workerImage(), "1"));
        }
        assertThrows(ServiceException.class,
                () -> FmGroovyVersionManifest.build(version(), "P1", List.of(commands().getFirst()), profile("a")));
        assertThrows(ServiceException.class,
                () -> FmGroovyVersionManifest.build(version(), "OTHER", commands(), profile("a")));
        FmGroovyVersionManifest.build(version(), "P1", commands(), profile("a"));
        assertThrows(ServiceException.class, () -> new FmBpmnDesignValidator().validate(XML, "P1"));
    }

    @Test
    void runtimeVerificationRejectsDraftTamperingAndProfileSubstitution() throws Exception {
        var version = version();
        var manifest = FmGroovyVersionManifest.build(version, "P1", commands(), profile("a"));
        version.setBpmnSha256(manifest.bpmnSha256());
        version.setVersionStatus("DRAFT");
        assertThrows(IllegalArgumentException.class, () -> FmGroovyVersionManifest.verify(version,
                "P1", commands(), profile("a"), manifest.canonicalJson(), manifest.sha256()));
        for (String status : List.of("PUBLISHED", "RETIRED")) {
            version.setVersionStatus(status);
            assertEquals(manifest, FmGroovyVersionManifest.verify(version, "P1", commands(), profile("a"),
                    manifest.canonicalJson(), manifest.sha256()));
            assertThrows(IllegalArgumentException.class, () -> FmGroovyVersionManifest.verify(version,
                    "P1", commands(), profile("b"), manifest.canonicalJson(), manifest.sha256()));
            assertThrows(IllegalArgumentException.class, () -> FmGroovyVersionManifest.verify(version,
                    "P1", commands(), profile("a"), manifest.canonicalJson() + " ", manifest.sha256()));
        }
        version.setTenantId("OTHER");
        assertThrows(IllegalArgumentException.class, () -> FmGroovyVersionManifest.verify(version,
                "P1", commands(), profile("a"), manifest.canonicalJson(), manifest.sha256()));
    }

    private FmProcessVersion version() {
        FmProcessVersion version = new FmProcessVersion();
        version.setTenantId("T1");
        version.setProcessDefId("definition1");
        version.setVersionNo(1);
        version.setBpmnXml(XML);
        return version;
    }

    private List<FmGroovyBindingCommand> commands() {
        return List.of(binding("calculate", "calculation", "return [amount: input.amount]\n"),
                binding("convert", "conversion", "return [amount: 2]\n"));
    }

    private FmGroovyBindingCommand binding(String node, String binding, String script) {
        return new FmGroovyBindingCommand(node, binding, script, SCHEMA, SCHEMA, MAPPING, 3000);
    }

    private FmGroovyVersionManifest.EngineProfile profile(String hex) {
        return new FmGroovyVersionManifest.EngineProfile("5.0.6", "ExampleJDK", "21.0.10+7",
                "local/worker@sha256:" + hex.repeat(64), "1");
    }
}
