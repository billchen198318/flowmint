package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

class FmBpmnDesignValidatorTest {

    private final FmBpmnDesignValidator validator = new FmBpmnDesignValidator();

    @Test
    void extractsReferencedFormFields() throws Exception {
        String xml = "${flowmintFormData.totalAmount >= 1000 && "
                + "flowmintFormData.currency == \"TWD\"}";

        assertEquals(Set.of("totalAmount", "currency"),
                validator.referencedFormFields(xml));
    }

    @Test
    void acceptsControlledGatewayConditions() {
        assertDoesNotThrow(() -> validator.validate(process("""
                <exclusiveGateway id="gateway" default="flow_default" />
                <userTask id="approval" />
                <sequenceFlow id="flow_amount" sourceRef="gateway" targetRef="approval">
                  <conditionExpression xsi:type="tFormalExpression">${flowmintFormData.totalAmountTwd &gt; 300000}</conditionExpression>
                </sequenceFlow>
                <sequenceFlow id="flow_default" sourceRef="gateway" targetRef="end" />
                """), "TEST_PROCESS"));
    }

    @Test
    void rejectsServiceTask() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <serviceTask id="unsafe" flowable:delegateExpression="${unsafeBean}" />
                """, " xmlns:flowable=\"http://flowable.org/bpmn\""), "TEST_PROCESS"));
    }

    @Test
    void acceptsControlledDataActionTask() {
        assertDoesNotThrow(() -> validator.validate(process("""
                <serviceTask id="loadEmployee" name="Load employee"
                    flowmint:taskType="DATA_ACTION"
                    flowmint:actionCode="FM_GET_EMPLOYEE"
                    flowmint:actionVersion="1"
                    flowmint:requestMapping="{&quot;employeeId&quot;:&quot;FORM_DATA.employeeId&quot;}"
                    flowmint:responseMapping="{&quot;employee.name&quot;:&quot;FORM_DATA.employeeName&quot;}" />
                """, " xmlns:flowmint=\"https://flowmint.qifu.org/schema/bpmn\""),
                "TEST_PROCESS"));
    }

    @Test
    void rejectsDataActionTaskWithArbitraryRequestMapping() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <serviceTask id="unsafe"
                    flowmint:taskType="DATA_ACTION"
                    flowmint:actionCode="FM_GET_EMPLOYEE"
                    flowmint:actionVersion="1"
                    flowmint:requestMapping="{&quot;employeeId&quot;:&quot;${unsafeBean.execute()}&quot;}"
                    flowmint:responseMapping="{}" />
                """, " xmlns:flowmint=\"https://flowmint.qifu.org/schema/bpmn\""),
                "TEST_PROCESS"));
    }

    @Test
    void rejectsForeignServiceTaskExtensionAttributes() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <serviceTask id="unsafe"
                    flowmint:taskType="DATA_ACTION"
                    flowmint:actionCode="FM_GET_EMPLOYEE"
                    flowmint:actionVersion="1"
                    flowmint:requestMapping="{}"
                    flowmint:responseMapping="{}"
                    vendor:async="true" />
                """, " xmlns:flowmint=\"https://flowmint.qifu.org/schema/bpmn\""
                        + " xmlns:vendor=\"https://example.invalid/bpmn\""),
                "TEST_PROCESS"));
    }

    @Test
    void rejectsUnknownProcessContextAndEmptyFormPath() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <serviceTask id="badContext"
                    flowmint:taskType="DATA_ACTION"
                    flowmint:actionCode="FM_GET_EMPLOYEE"
                    flowmint:actionVersion="1"
                    flowmint:requestMapping="{&quot;employeeId&quot;:&quot;PROCESS_CONTEXT.wrongName&quot;}"
                    flowmint:responseMapping="{}" />
                """, " xmlns:flowmint=\"https://flowmint.qifu.org/schema/bpmn\""),
                "TEST_PROCESS"));
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <serviceTask id="emptyPath"
                    flowmint:taskType="DATA_ACTION"
                    flowmint:actionCode="FM_GET_EMPLOYEE"
                    flowmint:actionVersion="1"
                    flowmint:requestMapping="{}"
                    flowmint:responseMapping="{&quot;employee.name&quot;:&quot;FORM_DATA.&quot;}" />
                """, " xmlns:flowmint=\"https://flowmint.qifu.org/schema/bpmn\""),
                "TEST_PROCESS"));
    }

    @Test
    void rejectsArbitraryExpression() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <exclusiveGateway id="gateway" default="flow_default" />
                <userTask id="approval" />
                <sequenceFlow id="flow_amount" sourceRef="gateway" targetRef="approval">
                  <conditionExpression xsi:type="tFormalExpression">${unsafeBean.execute()}</conditionExpression>
                </sequenceFlow>
                <sequenceFlow id="flow_default" sourceRef="gateway" targetRef="end" />
                """), "TEST_PROCESS"));
    }

    @Test
    void rejectsGatewayWithoutDefaultFlow() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <exclusiveGateway id="gateway" />
                <userTask id="approval" />
                <sequenceFlow id="flow_amount" sourceRef="gateway" targetRef="approval">
                  <conditionExpression xsi:type="tFormalExpression">${flowmintFormData.totalAmountTwd &gt; 300000}</conditionExpression>
                </sequenceFlow>
                <sequenceFlow id="flow_other" sourceRef="gateway" targetRef="end">
                  <conditionExpression xsi:type="tFormalExpression">${flowmintFormData.totalAmountTwd &lt;= 300000}</conditionExpression>
                </sequenceFlow>
                """), "TEST_PROCESS"));
    }

    @Test
    void rejectsConditionOnParallelGatewayOutgoingFlow() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <parallelGateway id="parallel" />
                <userTask id="approval" />
                <sequenceFlow id="flow_conditional" sourceRef="parallel" targetRef="approval">
                  <conditionExpression xsi:type="tFormalExpression">${flowmintFormData.totalAmountTwd &gt; 300000}</conditionExpression>
                </sequenceFlow>
                <sequenceFlow id="flow_end" sourceRef="parallel" targetRef="end" />
                """), "TEST_PROCESS"));
    }

    private String process(String body) {
        return process(body, "");
    }

    private String process(String body, String extraNamespaces) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"%s
                    targetNamespace="FlowMint">
                  <process id="TEST_PROCESS" isExecutable="true">
                    <startEvent id="start" />
                    %s
                    <endEvent id="end" />
                  </process>
                </definitions>
                """.formatted(extraNamespaces, body);
    }
}
