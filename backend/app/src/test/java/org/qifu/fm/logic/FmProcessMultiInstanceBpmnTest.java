package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.stream.XMLInputFactory;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.UserTask;
import org.flowable.bpmn.model.ServiceTask;
import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.logic.impl.FmProcessDefLogicServiceImpl;
import org.qifu.fm.domain.workflow.FmDataActionTaskPublishValidator;
import org.qifu.fm.service.IFmTaskPolicyService;

class FmProcessMultiInstanceBpmnTest {

    @Test
    void convertsAllAndSequentialPoliciesToRealMultiInstanceTasks() throws Exception {
        IFmTaskPolicyService policies = mock(IFmTaskPolicyService.class);
        when(policies.findByVersion("T1", "P1", 1)).thenReturn(List.of(
                policy("candidateApproval", "CANDIDATE"),
                policy("allApproval", "ALL"),
                policy("orderedApproval", "SEQUENTIAL")));
        FmProcessDefLogicServiceImpl service = new FmProcessDefLogicServiceImpl(
                null, null, null, null, null, policies, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                mock(FmDataActionTaskPublishValidator.class));
        FmProcessVersion version = new FmProcessVersion();
        version.setTenantId("T1");
        version.setProcessDefId("P1");
        version.setVersionNo(1);
        version.setBpmnXml(bpmn());

        Method method = FmProcessDefLogicServiceImpl.class
                .getDeclaredMethod("runtimeBpmnXml", FmProcessVersion.class);
        method.setAccessible(true);
        String runtimeXml = (String) method.invoke(service, version);
        BpmnModel model = new BpmnXMLConverter().convertToBpmnModel(
                XMLInputFactory.newFactory().createXMLStreamReader(
                        new StringReader(runtimeXml)));

        UserTask all = (UserTask) model.getMainProcess().getFlowElement("allApproval");
        UserTask candidate = (UserTask) model.getMainProcess()
                .getFlowElement("candidateApproval");
        UserTask ordered = (UserTask) model.getMainProcess()
                .getFlowElement("orderedApproval");
        for (UserTask task : List.of(candidate, all, ordered)) {
            assertTrue(task.getTaskListeners().stream()
                    .anyMatch(listener -> "create".equals(listener.getEvent())
                            && "${fmTaskAssignmentListener}"
                                    .equals(listener.getImplementation())));
        }
        assertNotNull(all.getLoopCharacteristics());
        assertNull(candidate.getLoopCharacteristics());
        assertFalse(all.getLoopCharacteristics().isSequential());
        assertTrue(ordered.getLoopCharacteristics().isSequential());
        assertEquals("flowmintAssignee",
                ordered.getLoopCharacteristics().getElementVariable());
        String collection = ordered.getLoopCharacteristics().getCollectionString();
        if (collection == null) {
            collection = ordered.getLoopCharacteristics().getInputDataItem();
        }
        assertNotNull(collection);
        assertTrue(collection.contains("multiInstanceAccounts"));
    }

    @Test
    void convertsDataActionTaskToFixedAsyncDelegate() throws Exception {
        IFmTaskPolicyService policies = mock(IFmTaskPolicyService.class);
        when(policies.findByVersion("T1", "P1", 1)).thenReturn(List.of());
        FmDataActionTaskPublishValidator validator =
                mock(FmDataActionTaskPublishValidator.class);
        doAnswer(invocation -> {
            BpmnModel validated = invocation.getArgument(1);
            ((ServiceTask) validated.getMainProcess().getFlowElement("loadEmployee"))
                    .setFailedJobRetryTimeCycleValue("R0/PT1M");
            return null;
        }).when(validator).validate(org.mockito.ArgumentMatchers.eq("T1"),
                org.mockito.ArgumentMatchers.any(BpmnModel.class));
        FmProcessDefLogicServiceImpl service = new FmProcessDefLogicServiceImpl(
                null, null, null, null, null, policies, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                validator);
        FmProcessVersion version = new FmProcessVersion();
        version.setTenantId("T1");
        version.setProcessDefId("P1");
        version.setVersionNo(1);
        version.setBpmnXml("""
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:flowmint="https://flowmint.qifu.org/schema/bpmn"
                  targetNamespace="FlowMint">
                  <process id="dataActionTest" isExecutable="true">
                    <startEvent id="start" />
                    <serviceTask id="loadEmployee"
                      flowmint:taskType="DATA_ACTION"
                      flowmint:actionCode="FM_GET_EMPLOYEE"
                      flowmint:actionVersion="1"
                      flowmint:requestMapping="{}"
                      flowmint:responseMapping="{}" />
                    <endEvent id="end" />
                    <sequenceFlow id="f1" sourceRef="start" targetRef="loadEmployee" />
                    <sequenceFlow id="f2" sourceRef="loadEmployee" targetRef="end" />
                  </process>
                </definitions>
                """);

        Method method = FmProcessDefLogicServiceImpl.class
                .getDeclaredMethod("runtimeBpmnXml", FmProcessVersion.class);
        method.setAccessible(true);
        String runtimeXml = (String) method.invoke(service, version);
        BpmnModel model = new BpmnXMLConverter().convertToBpmnModel(
                XMLInputFactory.newFactory().createXMLStreamReader(
                        new StringReader(runtimeXml)));

        ServiceTask task = (ServiceTask) model.getMainProcess()
                .getFlowElement("loadEmployee");
        assertEquals("${fmDataActionTaskDelegate}", task.getImplementation());
        assertTrue(task.isAsynchronous());
        assertEquals("R0/PT1M", task.getFailedJobRetryTimeCycleValue());
        assertTrue(task.getFieldExtensions().stream()
                .anyMatch(field -> "flowmintActionCode".equals(field.getFieldName())
                        && "FM_GET_EMPLOYEE".equals(field.getStringValue())));
    }

    private FmTaskPolicy policy(String taskKey, String mode) {
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setTaskDefKey(taskKey);
        policy.setAssignmentMode(mode);
        return policy;
    }

    private String bpmn() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  targetNamespace="FlowMint">
                  <process id="multiTest" isExecutable="true">
                    <startEvent id="start" />
                    <userTask id="candidateApproval" name="Candidate" />
                    <userTask id="allApproval" name="All" />
                    <userTask id="orderedApproval" name="Ordered" />
                    <endEvent id="end" />
                    <sequenceFlow id="f1" sourceRef="start" targetRef="candidateApproval" />
                    <sequenceFlow id="f2" sourceRef="candidateApproval" targetRef="allApproval" />
                    <sequenceFlow id="f3" sourceRef="allApproval" targetRef="orderedApproval" />
                    <sequenceFlow id="f4" sourceRef="orderedApproval" targetRef="end" />
                  </process>
                </definitions>
                """;
    }
}
