package org.qifu.fm.logic;

import java.time.Instant;
import java.util.List;

import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeMapping.TrustedContext;
import org.qifu.fm.domain.workflow.FmGroovyRuntimePreparation;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessVersion;

final class GroovyRuntimeFixture {

    static final Instant START = Instant.parse("2026-09-12T00:00:00Z");
    static final FmGroovyVersionManifest.EngineProfile PROFILE = new FmGroovyVersionManifest.EngineProfile(
            "5.0.6", "ExampleJDK", "21.0.10+7", "local/worker@sha256:" + "a".repeat(64), "1");
    static final String FORM_DATA = "{\"amount\":12.5,\"applicantAccount\":\"forged-form-account\"}";
    static final String FORM = """
            {"components":[
                {"type":"number","key":"amount","validate":{"required":true}},
                {"type":"number","key":"total","properties":{"flowmintSystemTaskWritable":true}},
                {"type":"textfield","key":"applicantAccount"}
            ]}
            """;

    private GroovyRuntimeFixture() {
    }

    static FmGroovyRuntimePreparation preparation() throws Exception {
        var version = new FmProcessVersion();
        version.setTenantId("T");
        version.setProcessDefId("P");
        version.setVersionNo(1);
        version.setVersionStatus("RETIRED");
        version.setBpmnXml("""
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
                """);
        var bindings = List.of(new FmGroovyBindingCommand("calculate", "calculation",
                "return [total: input.amount * 2G]",
                """
                {"type":"object","required":["amount"],"properties":{"amount":{"type":"number"}}}
                """,
                """
                {"type":"object","required":["total"],"properties":{"total":{"type":"number"}}}
                """,
                """
                {"mappingVersion":1,"input":{"amount":{"source":"FORM_DATA","path":"amount"}},
                    "output":{"total":{"target":"FORM_DATA","path":"total"}}}
                """, 3000));
        var manifest = FmGroovyVersionManifest.build(version, "P", bindings, PROFILE);
        version.setBpmnSha256(manifest.bpmnSha256());
        return new FmGroovyRuntimePreparation(version, "P", bindings, PROFILE,
                manifest.canonicalJson(), manifest.sha256(), "calculate", FORM);
    }

    static IFmGroovyRuntimeInputLogicService.LoadedInput loaded() throws Exception {
        return loaded("invocation", START);
    }

    static IFmGroovyRuntimeInputLogicService.LoadedInput loaded(String invocation, Instant startedAt) throws Exception {
        var preparation = preparation();
        var context = new TrustedContext("T", "P", 1, "instance", "calculate", invocation, startedAt,
                "business", "DOC001", "applicant", "initiator", "ORG");
        return new IFmGroovyRuntimeInputLogicService.LoadedInput("T", "execution", "instance", "P", 1,
                "calculate", "data", 4, FORM_DATA, preparation.input(FORM_DATA, context, 3), preparation);
    }

    static <T> DefaultResult<List<T>> result(List<T> values) {
        var result = new DefaultResult<List<T>>();
        result.setValue(values);
        return result;
    }
}
