package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessVersion;

class FmGroovyDraftValidatorTest {

    private static final String XML = """
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                xmlns:fm="https://flowmint.qifu.org/schema/bpmn" targetNamespace="test">
                <process id="P1" isExecutable="true">
                    <startEvent id="start" />
                    <serviceTask id="calculate" fm:taskType="GROOVY" fm:bindingId="calculate-binding" />
                    <endEvent id="end" />
                    <sequenceFlow id="f1" sourceRef="start" targetRef="calculate" />
                    <sequenceFlow id="f2" sourceRef="calculate" targetRef="end" />
                </process>
            </definitions>
            """;
    private static final String SCHEMA = """
            {"type":"object","properties":{"amount":{"type":"number"}}}
            """;
    private static final String MAPPING = """
            {"mappingVersion":1,
             "input":{"amount":{"source":"FORM_DATA","path":"totalAmount"}},
             "output":{"amount":{"target":"FORM_DATA","path":"calculatedAmount"}}}
            """;

    @Test
    void draftGateNeverEnablesPublication() {
        FmBpmnDesignValidator validator = new FmBpmnDesignValidator();
        assertDoesNotThrow(() -> validator.validateDraft(XML, "P1", true));
        assertThrows(ServiceException.class, () -> validator.validateDraft(XML, "P1", false));
        assertThrows(ServiceException.class, () -> validator.validate(XML, "P1"));
    }

    @Test
    void rejectsExecutionAttributesEvenInDraftMode() {
        String injected = XML.replace("fm:bindingId=", "expression=\"evil\" fm:bindingId=");
        assertThrows(ServiceException.class,
                () -> new FmBpmnDesignValidator().validateDraft(injected, "P1", true));
    }

    @Test
    void bindsTenantAndVersionFromServerAndNormalizesScript() throws Exception {
        var result = new FmGroovyDraftValidator().validate(version(), XML,
                List.of(command(MAPPING, "return [amount: input.amount]\r\n")));
        assertEquals("T1", result.getFirst().getTenantId());
        assertEquals(1, result.getFirst().getVersionNo());
        assertEquals("return [amount: input.amount]\n", result.getFirst().getScriptContent());
        assertTrue(result.getFirst().getContentSha256().matches("[a-f0-9]{64}"));
    }

    @Test
    void rejectsXmlWithoutBindingAndOrphanBinding() {
        FmGroovyDraftValidator validator = new FmGroovyDraftValidator();
        assertThrows(ServiceException.class, () -> validator.validate(version(), XML, List.of()));
        assertThrows(ServiceException.class, () -> validator.validate(version(), XML,
                List.of(command(MAPPING, "return [:]"), command(MAPPING, "return [:]"))));
    }

    @Test
    void rejectsSystemFieldWritesAndUnknownContext() {
        FmGroovyDraftValidator validator = new FmGroovyDraftValidator();
        assertThrows(ServiceException.class, () -> validator.validate(version(), XML,
                List.of(command(MAPPING.replace("calculatedAmount", "tenantId"), "return [:]"))));
        assertThrows(ServiceException.class, () -> validator.validate(version(), XML,
                List.of(command(MAPPING.replace("FORM_DATA\",\"path\":\"totalAmount",
                        "PROCESS_CONTEXT\",\"path\":\"currentUser"), "return [:]"))));
    }

    @Test
    void rejectsUnknownMappingKeysAndExpressionPaths() {
        FmGroovyDraftValidator validator = new FmGroovyDraftValidator();
        assertThrows(ServiceException.class, () -> validator.validate(version(), XML,
                List.of(command(MAPPING.replace("totalAmount", "items[0].amount"), "return [:]"))));
        assertThrows(ServiceException.class, () -> validator.validate(version(), XML,
                List.of(command(MAPPING.replace("\"mappingVersion\":1", "\"mappingVersion\":1,\"script\":\"evil\""), "return [:]"))));
    }

    @Test
    void rejectsMalformedSchemaAndMistypedConstantOnSave() {
        var validator = new FmGroovyDraftValidator();
        for (String schema : List.of(SCHEMA.replace("\"number\"", "\"unknown\""),
                SCHEMA.replace("\"number\"", "\"array\""),
                SCHEMA.replace("\"type\":\"object\"", "\"type\":\"object\",\"required\":\"amount\""),
                SCHEMA + " {}")) {
            var command = new FmGroovyBindingCommand("calculate", "calculate-binding", "return [:]",
                    schema, SCHEMA, MAPPING, 3000);
            assertThrows(ServiceException.class, () -> validator.validate(version(), XML, List.of(command)));
        }
        String invalidConstant = MAPPING.replace("\"source\":\"FORM_DATA\",\"path\":\"totalAmount\"",
                "\"source\":\"CONSTANT\",\"value\":\"12.50\"");
        assertThrows(ServiceException.class,
                () -> validator.validate(version(), XML, List.of(command(invalidConstant, "return [:]"))));
    }

    private FmProcessVersion version() {
        FmProcessVersion version = new FmProcessVersion();
        version.setTenantId("T1");
        version.setProcessDefId("P1");
        version.setVersionNo(1);
        return version;
    }

    private FmGroovyBindingCommand command(String mapping, String script) {
        return new FmGroovyBindingCommand("calculate", "calculate-binding", script, SCHEMA, SCHEMA, mapping, 3000);
    }
}
