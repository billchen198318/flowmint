package org.qifu.fm.logic.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeMapping.TrustedContext;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest.EngineProfile;
import org.qifu.fm.logic.IFmGroovyRuntimeDefinitionLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads current persisted identity without taking form locks or executing scripts. */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class FmGroovyRuntimeInputLogicServiceImpl implements IFmGroovyRuntimeInputLogicService {

    private final RuntimeService runtime;
    private final RepositoryService repository;
    private final IFmProcessInstanceService instances;
    private final IFmFormDataService forms;
    private final IFmProcessVersionService versions;
    private final IFmGroovyRuntimeDefinitionLogicService definitions;

    public FmGroovyRuntimeInputLogicServiceImpl(RuntimeService runtime, RepositoryService repository,
            IFmProcessInstanceService instances, IFmFormDataService forms, IFmProcessVersionService versions,
            IFmGroovyRuntimeDefinitionLogicService definitions) {
        this.runtime = runtime;
        this.repository = repository;
        this.instances = instances;
        this.forms = forms;
        this.versions = versions;
        this.definitions = definitions;
    }

    @Override
    public LoadedInput load(String tenantId, String executionId, String invocationId, Instant startedAt,
            EngineProfile profile) throws ServiceException {
        require(text(tenantId) && text(executionId) && invocationId != null
                && invocationId.matches("[A-Za-z0-9_-]{1,64}") && startedAt != null && profile != null);
        var execution = runtime.createExecutionQuery().executionId(executionId).singleResult();
        require(execution != null && executionId.equals(execution.getId())
                && tenantId.equals(execution.getTenantId()) && !execution.isEnded() && !execution.isSuspended()
                && text(execution.getProcessInstanceId()) && text(execution.getActivityId()));
        String instanceId = execution.getProcessInstanceId();
        var engineInstance = runtime.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        require(engineInstance != null && instanceId.equals(engineInstance.getId())
                && tenantId.equals(engineInstance.getTenantId()) && !engineInstance.isEnded()
                && !engineInstance.isSuspended() && text(engineInstance.getProcessDefinitionId()));
        var instance = one(instances.selectListByParams(
                Map.of("tenantId", tenantId, "processInstanceId", instanceId)).getValue());
        require(tenantId.equals(instance.getTenantId()) && instanceId.equals(instance.getProcessInstanceId())
                && "RUNNING".equals(instance.getInstanceStatus()) && instance.getEndDate() == null
                && text(instance.getProcessDefId()) && positive(instance.getProcessVersionNo())
                && text(instance.getFormDataId()) && text(instance.getBusinessKey())
                && instance.getBusinessKey().equals(engineInstance.getBusinessKey())
                && engineInstance.getProcessDefinitionId().equals(instance.getFlowableProcessDefId()));
        var version = one(versions.selectListByParams(Map.of("tenantId", tenantId,
                "processDefId", instance.getProcessDefId(), "versionNo", instance.getProcessVersionNo())).getValue());
        require(tenantId.equals(version.getTenantId()) && instance.getProcessDefId().equals(version.getProcessDefId())
                && instance.getProcessVersionNo().equals(version.getVersionNo())
                && instance.getFlowableProcessDefId().equals(version.getFlowableProcessDefId())
                && ("PUBLISHED".equals(version.getVersionStatus()) || "RETIRED".equals(version.getVersionStatus())));
        var form = one(forms.selectListByParams(
                Map.of("tenantId", tenantId, "formDataId", instance.getFormDataId())).getValue());
        require(tenantId.equals(form.getTenantId()) && instance.getFormDataId().equals(form.getFormDataId())
                && instance.getBusinessKey().equals(form.getBusinessKey())
                && Objects.equals(instance.getDocumentNumber(), form.getDocumentNumber())
                && "SUBMITTED".equals(form.getDataStatus()) && text(form.getFormId())
                && positive(form.getFormVersionNo()) && positive(form.getRevisionNo())
                && form.getLockVersion() != null && form.getLockVersion() >= 0);
        var model = repository.getBpmnModel(instance.getFlowableProcessDefId());
        require(model != null);
        var element = model.getFlowElement(execution.getActivityId());
        require(element instanceof ServiceTask);
        var task = (ServiceTask) element;
        require(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(task.getImplementationType())
                && "${fmGroovyTaskDelegate}".equals(task.getImplementation())
                && task.isAsynchronous() && task.isExclusive()
                && "R0/PT1M".equals(task.getFailedJobRetryTimeCycleValue())
                && task.getFieldExtensions().size() == 2);
        var preparation = definitions.load(tenantId, instance.getProcessDefId(), instance.getProcessVersionNo(),
                task.getId(), form.getFormId(), form.getFormVersionNo(), profile);
        require(preparation.bindingId().equals(field(task, "flowmintBindingId"))
                && preparation.bindingSha256().equals(field(task, "flowmintBindingSha256")));
        try {
            var context = new TrustedContext(tenantId, instance.getProcessDefId(), instance.getProcessVersionNo(),
                    instanceId, task.getId(), invocationId, startedAt, instance.getBusinessKey(),
                    instance.getDocumentNumber(), form.getOwnerAccount(), instance.getInitiatorAccount(),
                    form.getOwnerOrgUnitId());
            var snapshot = preparation.input(form.getDataContent(), context, form.getRevisionNo());
            return new LoadedInput(tenantId, executionId, instanceId, instance.getProcessDefId(),
                    instance.getProcessVersionNo(), task.getId(), form.getFormDataId(), form.getLockVersion(),
                    form.getDataContent(), snapshot, preparation);
        } catch (IllegalArgumentException invalid) {
            throw new ServiceException("GROOVY_RUNTIME_INPUT_INVALID");
        }
    }

    private static String field(ServiceTask task, String name) throws ServiceException {
        var fields = task.getFieldExtensions().stream().filter(field -> name.equals(field.getFieldName())).toList();
        require(fields.size() == 1 && fields.getFirst().getExpression() == null
                && text(fields.getFirst().getStringValue()));
        return fields.getFirst().getStringValue();
    }

    private static boolean text(String value) {
        return value != null && !value.isBlank() && value.length() <= 200;
    }

    private static boolean positive(Integer value) {
        return value != null && value > 0;
    }

    private static <T> T one(List<T> values) throws ServiceException {
        require(values != null && values.size() == 1 && values.getFirst() != null);
        return values.getFirst();
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_RUNTIME_IDENTITY_INVALID");
        }
    }
}
