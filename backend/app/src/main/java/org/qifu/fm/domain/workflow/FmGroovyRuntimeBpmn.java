package org.qifu.fm.domain.workflow;

import java.io.StringReader;
import java.util.HashSet;

import javax.xml.stream.XMLInputFactory;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.ServiceTask;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyPublicationCompiler.VerifiedPublication;

/** Prepares Groovy tasks from the exact design checked by the publication compiler. */
public final class FmGroovyRuntimeBpmn {

    private FmGroovyRuntimeBpmn() {
    }

    public static BpmnModel prepare(String designXml, VerifiedPublication evidence) throws ServiceException {
        if (designXml == null || evidence == null
                || !FmGroovyContractJson.sha256(designXml).equals(evidence.manifest().bpmnSha256())) {
            throw new ServiceException("GROOVY_PUBLICATION_STALE_CONTENT");
        }
        try {
            var factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            var reader = factory.createXMLStreamReader(new StringReader(designXml));
            var converter = new BpmnXMLConverter();
            BpmnModel model;
            try {
                model = converter.convertToBpmnModel(reader);
            } finally {
                reader.close();
            }
            var converted = new HashSet<String>();
            for (ServiceTask task : model.getMainProcess().findFlowElementsOfType(ServiceTask.class, true)) {
                if (!"GROOVY".equals(task.getAttributeValue(
                        FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE, "taskType"))) {
                    continue;
                }
                var digest = evidence.manifest().bindings().stream()
                        .filter(binding -> binding.nodeId().equals(task.getId()))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Missing binding"));
                if (!digest.bindingId().equals(task.getAttributeValue(
                        FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE, "bindingId"))
                        || !converted.add(digest.bindingId()) || !task.getFieldExtensions().isEmpty()) {
                    throw new IllegalArgumentException("Invalid binding");
                }
                task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
                task.setImplementation("${fmGroovyTaskDelegate}");
                task.setAsynchronous(true);
                task.setExclusive(true);
                // The platform owns the retry budget; Flowable must not multiply attempts.
                task.setFailedJobRetryTimeCycleValue("R0/PT1M");
                addField(task, "flowmintBindingId", digest.bindingId());
                addField(task, "flowmintBindingSha256", digest.sha256());
            }
            if (converted.size() != evidence.manifest().bindings().size()) {
                throw new IllegalArgumentException("Incomplete bindings");
            }
            // The publisher must apply User Task and Data Action transformations to this same model
            // before serializing it. An intermediate XML round trip loses custom design attributes.
            return model;
        } catch (Exception invalid) {
            throw new ServiceException("GROOVY_RUNTIME_BPMN_INVALID");
        }
    }

    private static void addField(ServiceTask task, String name, String value) {
        var field = new FieldExtension();
        field.setFieldName(name);
        field.setStringValue(value);
        task.getFieldExtensions().add(field);
    }
}
