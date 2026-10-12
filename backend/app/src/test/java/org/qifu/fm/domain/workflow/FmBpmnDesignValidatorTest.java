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
        String xml = process("""
                <sequenceFlow id="flow_cond" sourceRef="start" targetRef="end">
                  <conditionExpression xsi:type="tFormalExpression">${flowmintFormData.totalAmount &gt;= 1000 &amp;&amp; flowmintFormData.currency == "TWD"}</conditionExpression>
                </sequenceFlow>
                """);

        assertEquals(Set.of("totalAmount", "currency"),
                validator.referencedFormFields(xml));
    }

    @Test
    void acceptsControlledGatewayConditions() {
        assertDoesNotThrow(() -> validator.validate(process("""
                <exclusiveGateway id="gateway" default="flow_default" />
                <userTask id="approval" />
                <sequenceFlow id="flow_start" sourceRef="start" targetRef="gateway" />
                <sequenceFlow id="flow_amount" sourceRef="gateway" targetRef="approval">
                  <conditionExpression xsi:type="tFormalExpression">${flowmintFormData.totalAmountTwd &gt; 300000}</conditionExpression>
                </sequenceFlow>
                <sequenceFlow id="flow_approval_end" sourceRef="approval" targetRef="end" />
                <sequenceFlow id="flow_default" sourceRef="gateway" targetRef="end" />
                """), "TEST_PROCESS"));
    }

    @Test
    void rejectsDisconnectedUserTask() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <userTask id="isolatedApproval" />
                <sequenceFlow id="flow_start_end" sourceRef="start" targetRef="end" />
                """), "TEST_PROCESS"));
    }

    @Test
    void rejectsUserTaskWithoutPathToEnd() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <userTask id="deadEndApproval" />
                <sequenceFlow id="flow_start_task" sourceRef="start" targetRef="deadEndApproval" />
                <sequenceFlow id="flow_start_end" sourceRef="start" targetRef="end" />
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
                <sequenceFlow id="flow_start_action" sourceRef="start" targetRef="loadEmployee" />
                <sequenceFlow id="flow_action_end" sourceRef="loadEmployee" targetRef="end" />
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
    void ignoresFieldNamesOutsideConditionExpressions() throws Exception {
        String xml = process("""
                <documentation>flowmintFormData.oldField</documentation>
                <exclusiveGateway id="gateway" name="flowmintFormData.nodeName" default="flow_default" />
                <userTask id="approval" />
                <sequenceFlow id="flow_cond" sourceRef="gateway" targetRef="approval">
                  <conditionExpression xsi:type="tFormalExpression">${flowmintFormData.amount &gt; 100}</conditionExpression>
                </sequenceFlow>
                <sequenceFlow id="flow_default" sourceRef="gateway" targetRef="end" />
                """);
        assertEquals(Set.of("amount"), validator.referencedFormFields(xml));
    }

    @Test
    void rejectsMixedGatewayOperatorsRegardlessOfWhitespace() {
        for (String expression : Set.of(
                "${flowmintFormData.a==1&amp;&amp;flowmintFormData.b==2||flowmintFormData.c==3}",
                "${flowmintFormData.a==1 &amp;&amp; flowmintFormData.b==2||flowmintFormData.c==3}")) {
            assertThrows(ServiceException.class, () -> validator.validate(process("""
                    <exclusiveGateway id="gateway" default="flow_default" />
                    <userTask id="approval" />
                    <sequenceFlow id="flow_cond" sourceRef="gateway" targetRef="approval">
                      <conditionExpression xsi:type="tFormalExpression">%s</conditionExpression>
                    </sequenceFlow>
                    <sequenceFlow id="flow_default" sourceRef="gateway" targetRef="end" />
                    """.formatted(expression)), "TEST_PROCESS"));
        }
    }

    @Test
    void logicalOperatorTextInsideStringValueDoesNotCountAsMixing() {
        assertDoesNotThrow(() -> validator.validate(process("""
                <exclusiveGateway id="gateway" default="flow_default" />
                <userTask id="approval" />
                <sequenceFlow id="flow_start" sourceRef="start" targetRef="gateway" />
                <sequenceFlow id="flow_cond" sourceRef="gateway" targetRef="approval">
                  <conditionExpression xsi:type="tFormalExpression">${flowmintFormData.note=="a||b"&amp;&amp;flowmintFormData.a==1}</conditionExpression>
                </sequenceFlow>
                <sequenceFlow id="flow_approval_end" sourceRef="approval" targetRef="end" />
                <sequenceFlow id="flow_default" sourceRef="gateway" targetRef="end" />
                """), "TEST_PROCESS"));
    }

    @Test
    void rejectsUserTaskDispatchAndExecutionAttributes() {
        for (String attribute : Set.of(
                "flowable:assignee=\"otherUser\"",
                "flowable:candidateUsers=\"otherUser\"",
                "flowable:async=\"true\"")) {
            assertThrows(ServiceException.class, () -> validator.validate(process(
                    "<userTask id=\"approval\" " + attribute + " />",
                    " xmlns:flowable=\"http://flowable.org/bpmn\""), "TEST_PROCESS"));
        }
    }

    @Test
    void rejectsStartEventExecutionAttributes() {
        String xml = process("", " xmlns:flowable=\"http://flowable.org/bpmn\"")
                .replace("<startEvent id=\"start\" />",
                        "<startEvent id=\"start\" flowable:async=\"true\" />");
        assertThrows(ServiceException.class, () -> validator.validate(xml, "TEST_PROCESS"));
    }

    @Test
    void rejectsArbitraryConditionsOutsideMultiExitGateways() {
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <sequenceFlow id="flow_start" sourceRef="start" targetRef="end">
                  <conditionExpression xsi:type="tFormalExpression">${unsafeBean.execute()}</conditionExpression>
                </sequenceFlow>
                """), "TEST_PROCESS"));
        assertThrows(ServiceException.class, () -> validator.validate(process("""
                <exclusiveGateway id="gateway" />
                <sequenceFlow id="flow_start" sourceRef="start" targetRef="gateway" />
                <sequenceFlow id="flow_gateway" sourceRef="gateway" targetRef="end">
                  <conditionExpression xsi:type="tFormalExpression">${unsafeBean.execute()}</conditionExpression>
                </sequenceFlow>
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
