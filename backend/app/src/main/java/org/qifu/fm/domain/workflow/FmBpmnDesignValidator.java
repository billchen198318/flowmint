package org.qifu.fm.domain.workflow;

import java.io.StringReader;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.InclusiveGateway;
import org.flowable.bpmn.model.ParallelGateway;
import org.flowable.bpmn.model.SequenceFlow;
import org.qifu.base.exception.ServiceException;

public class FmBpmnDesignValidator {

    private static final String BPMN_NAMESPACE =
            "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private static final Set<String> ALLOWED_BPMN_ELEMENTS = Set.of(
            "definitions", "process", "documentation",
            "startEvent", "endEvent", "userTask",
            "exclusiveGateway", "inclusiveGateway", "parallelGateway",
            "sequenceFlow", "conditionExpression", "incoming", "outgoing");

    private static final String FIELD =
            "flowmintFormData\\.[A-Za-z][A-Za-z0-9_]*";

    private static final String VALUE =
            "(?:-?\\d+(?:\\.\\d+)?|true|false|\"(?:\\\\.|[^\"\\\\])*\")";

    private static final String TERM =
            FIELD + "\\s*(?:==|!=|>=|<=|>|<)\\s*" + VALUE;

    private static final Pattern CONDITION = Pattern.compile(
            "^\\$\\{\\s*" + TERM + "(?:\\s*(?:&&|\\|\\|)\\s*" + TERM
                    + ")*\\s*}$");

    private static final Pattern FIELD_REFERENCE = Pattern.compile(
            "flowmintFormData\\.([A-Za-z][A-Za-z0-9_]*)");

    public void validate(String xml, String expectedProcessKey) throws ServiceException {
        if (StringUtils.isAnyBlank(xml, expectedProcessKey)) {
            throw new ServiceException("BPMN XML 與流程代碼不可空白");
        }
        try {
            assertElementWhitelist(xml);
            XMLStreamReader reader = inputFactory().createXMLStreamReader(new StringReader(xml));
            BpmnModel model = new BpmnXMLConverter().convertToBpmnModel(reader);
            if (model.getProcesses().size() != 1
                    || !expectedProcessKey.equals(model.getMainProcess().getId())) {
                throw new ServiceException(
                        "BPMN 必須只有一個 Process，且 Process ID 必須等於流程代碼 "
                                + expectedProcessKey);
            }
            validateGateways(model);
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("BPMN XML 驗證失敗：" + exception.getMessage());
        }
    }

    public Set<String> referencedFormFields(String xml) throws ServiceException {
        if (StringUtils.isBlank(xml)) {
            throw new ServiceException("BPMN XML 不可為空");
        }
        Set<String> fields = new HashSet<>();
        Matcher matcher = FIELD_REFERENCE.matcher(xml);
        while (matcher.find()) {
            fields.add(matcher.group(1));
        }
        return Set.copyOf(fields);
    }

    private void assertElementWhitelist(String xml) throws Exception {
        XMLStreamReader reader = inputFactory().createXMLStreamReader(new StringReader(xml));
        while (reader.hasNext()) {
            if (reader.next() != XMLStreamConstants.START_ELEMENT) {
                continue;
            }
            if (BPMN_NAMESPACE.equals(reader.getNamespaceURI())
                    && !ALLOWED_BPMN_ELEMENTS.contains(reader.getLocalName())) {
                throw new ServiceException(
                        "流程設計不允許 BPMN 元件：" + reader.getLocalName());
            }
            String namespace = StringUtils.defaultString(reader.getNamespaceURI());
            if (!namespace.isEmpty()
                    && !BPMN_NAMESPACE.equals(namespace)
                    && !namespace.startsWith("http://www.omg.org/spec/BPMN/20100524/DI")
                    && !namespace.startsWith("http://www.omg.org/spec/DD/20100524/")) {
                throw new ServiceException("流程設計不允許擴充元件命名空間：" + namespace);
            }
        }
    }

    private XMLInputFactory inputFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        return factory;
    }

    private void validateGateways(BpmnModel model) throws ServiceException {
        for (FlowNode gateway : model.getMainProcess().findFlowElementsOfType(
                ExclusiveGateway.class)) {
            validateGateway(gateway, ((ExclusiveGateway) gateway).getDefaultFlow());
        }
        for (FlowNode gateway : model.getMainProcess().findFlowElementsOfType(
                InclusiveGateway.class)) {
            validateGateway(gateway, ((InclusiveGateway) gateway).getDefaultFlow());
        }
        for (ParallelGateway gateway : model.getMainProcess().findFlowElementsOfType(
                ParallelGateway.class)) {
            for (SequenceFlow flow : gateway.getOutgoingFlows()) {
                if (StringUtils.isNotBlank(flow.getConditionExpression())) {
                    throw new ServiceException(
                            "Parallel Gateway 的 Sequence Flow 不可設定條件：" + label(flow));
                }
            }
        }
    }

    private void validateGateway(FlowNode gateway, String defaultFlowId)
            throws ServiceException {
        if (gateway.getOutgoingFlows().size() <= 1) {
            return;
        }
        if (StringUtils.isBlank(defaultFlowId)) {
            throw new ServiceException("Gateway「" + label(gateway) + "」必須設定 Default Flow");
        }
        for (SequenceFlow flow : gateway.getOutgoingFlows()) {
            String expression = StringUtils.trimToEmpty(flow.getConditionExpression());
            if (flow.getId().equals(defaultFlowId)) {
                if (!expression.isEmpty()) {
                    throw new ServiceException(
                            "Default Flow「" + label(flow) + "」不可同時設定條件");
                }
                continue;
            }
            if (!CONDITION.matcher(expression).matches()
                    || (expression.contains(" && ") && expression.contains(" || "))) {
                throw new ServiceException(
                        "Sequence Flow「" + label(flow) + "」條件不是受控表單欄位格式");
            }
        }
    }

    private String label(org.flowable.bpmn.model.FlowElement element) {
        return StringUtils.defaultIfBlank(element.getName(), element.getId());
    }
}
