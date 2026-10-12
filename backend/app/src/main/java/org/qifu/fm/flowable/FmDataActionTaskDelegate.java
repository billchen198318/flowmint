package org.qifu.fm.flowable;

import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmDataActionTaskPublishValidator;
import org.qifu.fm.domain.workflow.FmGroovyContractJson;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.logic.IFmDataActionLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.qifu.fm.service.IFmFormVersionService;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component("fmDataActionTaskDelegate")
public class FmDataActionTaskDelegate implements JavaDelegate {

    private final IFmDataActionLogicService actionLogicService;
    private final IFmFormDataService formDataService;
    private final IFmFormSnapshotService formSnapshotService;
    private final IFmFormVersionService formVersionService;
    private final FmFormSubmissionValidator formSubmissionValidator;
    private final ObjectMapper objectMapper;

    public FmDataActionTaskDelegate(
            IFmDataActionLogicService actionLogicService,
            IFmFormDataService formDataService,
            IFmFormSnapshotService formSnapshotService,
            IFmFormVersionService formVersionService,
            FmFormSubmissionValidator formSubmissionValidator,
            ObjectMapper objectMapper) {
        this.actionLogicService = actionLogicService;
        this.formDataService = formDataService;
        this.formSnapshotService = formSnapshotService;
        this.formVersionService = formVersionService;
        this.formSubmissionValidator = formSubmissionValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(DelegateExecution execution) {
        try {
            ServiceTask task = (ServiceTask) execution.getCurrentFlowElement();
            String tenantId = requiredVariable(execution,
                    FmTaskAssignmentListener.VARIABLE_TENANT_ID);
            String actionCode = requiredAttribute(task, "actionCode");
            int actionVersion = Integer.parseInt(requiredAttribute(task, "actionVersion"));
            Map<String, Object> formData = mutableFormData(execution);
            Map<String, Object> request = request(task, execution, formData);
            String actor = requiredVariable(execution,
                    FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT);
            FmDataActionExecutionView result = actionLogicService.execute(
                    tenantId, actionCode, actionVersion, request, actor)
                    .getValueEmptyThrowMessage();
            Map<String, String> responseMapping = mapping(task, "responseMapping");
            if (!responseMapping.isEmpty()) {
                applyResponse(responseMapping, formData, result.data());
                persistFormData(tenantId, requiredVariable(execution,
                        FmTaskAssignmentListener.VARIABLE_FORM_DATA_ID),
                        execution.getProcessInstanceId(), formData);
                execution.setVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA, formData);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("FlowMint Data Action Task 執行失敗："
                    + exception.getMessage(), exception);
        }
    }

    private Map<String, Object> request(ServiceTask task, DelegateExecution execution,
            Map<String, Object> formData) throws Exception {
        Map<String, String> mapping = mapping(task, "requestMapping");
        Map<String, Object> request = new HashMap<>();
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String source = entry.getValue();
            Object value;
            if (source.startsWith("FORM_DATA.")) {
                value = path(formData, source.substring("FORM_DATA.".length()));
            } else if (source.startsWith("PROCESS_CONTEXT.")) {
                value = processContext(execution,
                        source.substring("PROCESS_CONTEXT.".length()));
            } else if (source.startsWith("CONSTANT:")) {
                value = source.substring("CONSTANT:".length());
            } else {
                throw new ServiceException("不支援的 Request Mapping：" + source);
            }
            request.put(entry.getKey(), value);
        }
        return request;
    }

    private void applyResponse(Map<String, String> responseMapping, Map<String, Object> formData,
            Map<String, Object> result) throws Exception {
        for (Map.Entry<String, String> entry
                : responseMapping.entrySet()) {
            String target = entry.getValue();
            if (!target.startsWith("FORM_DATA.")) {
                throw new ServiceException("不支援的 Response Mapping：" + target);
            }
            setPath(formData, target.substring("FORM_DATA.".length()),
                    path(result, entry.getKey()));
        }
    }

    private Object processContext(DelegateExecution execution, String name)
            throws ServiceException {
        return switch (name) {
            case "tenantId" -> execution.getVariable(
                    FmTaskAssignmentListener.VARIABLE_TENANT_ID);
            case "processDefId" -> execution.getVariable(
                    FmTaskAssignmentListener.VARIABLE_PROCESS_DEF_ID);
            case "processVersionNo" -> execution.getVariable(
                    FmTaskAssignmentListener.VARIABLE_PROCESS_VERSION_NO);
            case "initiatorAccount" -> execution.getVariable(
                    FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT);
            case "businessKey" -> execution.getProcessInstanceBusinessKey();
            case "processInstanceId" -> execution.getProcessInstanceId();
            default -> throw new ServiceException("不支援的 Process Context：" + name);
        };
    }

    private Map<String, Object> mutableFormData(DelegateExecution execution) throws Exception {
        Object value = execution.getVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA);
        if (value instanceof Map<?, ?> source) {
            return objectMapper.readValue(objectMapper.writeValueAsString(source),
                    new TypeReference<Map<String, Object>>() { });
        }
        return new HashMap<>();
    }

    private Map<String, String> mapping(ServiceTask task, String name) throws Exception {
        return objectMapper.readValue(
                StringUtils.defaultIfBlank(attribute(task, name), "{}"),
                new TypeReference<Map<String, String>>() { });
    }

    private Object path(Object source, String path) {
        Object value = source;
        for (String segment : path.split("\\.")) {
            if (!(value instanceof Map<?, ?> map)) return null;
            value = map.get(segment);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private void setPath(Map<String, Object> target, String path, Object value) {
        String[] segments = path.split("\\.");
        Map<String, Object> current = target;
        for (int index = 0; index < segments.length - 1; index++) {
            Object nested = current.get(segments[index]);
            if (!(nested instanceof Map<?, ?>)) {
                nested = new HashMap<String, Object>();
                current.put(segments[index], nested);
            }
            current = (Map<String, Object>) nested;
        }
        current.put(segments[segments.length - 1], value);
    }

    private void persistFormData(String tenantId, String formDataId,
            String processInstanceId,
            Map<String, Object> data) throws Exception {
        formDataService.lockByFormDataId(tenantId, formDataId);
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", tenantId);
        params.put("formDataId", formDataId);
        List<FmFormData> values = Objects.requireNonNullElse(
                formDataService.selectListByParams(params, "FORM_DATA_ID", "ASC").getValue(),
                List.of());
        FmFormData formData = values.stream().findFirst()
                .orElseThrow(() -> new ServiceException("找不到 System Task 對應的表單資料"));
        Map<String, Object> versionParams = new HashMap<>();
        versionParams.put("tenantId", tenantId);
        versionParams.put("formId", formData.getFormId());
        versionParams.put("versionNo", formData.getFormVersionNo());
        List<FmFormVersion> versions = Objects.requireNonNullElse(
                formVersionService.selectListByParams(versionParams).getValue(), List.of());
        FmFormVersion version = versions.stream().findFirst()
                .orElseThrow(() -> new ServiceException("找不到 System Task 對應的表單版本"));
        formSubmissionValidator.validate(version.getSchemaContent(), data);
        int expectedLockVersion = Objects.requireNonNullElse(formData.getLockVersion(), 0);
        String content = objectMapper.writeValueAsString(data);
        int updated = formDataService.updateDataContent(tenantId, formDataId,
                content, expectedLockVersion);
        if (updated != 1) {
            throw new ServiceException("System Task 更新表單資料時發生併發衝突");
        }
        List<FmFormData> updatedValues = Objects.requireNonNullElse(
                formDataService.selectListByParams(params, "FORM_DATA_ID", "ASC").getValue(),
                List.of());
        FmFormData saved = updatedValues.stream().findFirst()
                .orElseThrow(() -> new ServiceException("System Task 寫回後找不到表單資料"));
        if (!Objects.equals(saved.getRevisionNo(), formData.getRevisionNo() + 1)
                || !Objects.equals(saved.getLockVersion(), expectedLockVersion + 1)
                || !content.equals(saved.getDataContent())) {
            throw new ServiceException("System Task 寫回版本不一致");
        }
        FmFormSnapshot snapshot = new FmFormSnapshot();
        snapshot.setTenantId(tenantId);
        snapshot.setFormDataId(formDataId);
        snapshot.setProcessInstanceId(processInstanceId);
        snapshot.setActionType("SYSTEM_TASK_APPLY");
        snapshot.setFormVersionNo(saved.getFormVersionNo());
        snapshot.setRevisionNo(saved.getRevisionNo());
        snapshot.setDataContent(content);
        snapshot.setContentSha256(FmGroovyContractJson.sha256(content));
        snapshot.setSnapshotDate(new Date());
        String snapshotOid = formSnapshotService.insertSystemTaskSnapshot(snapshot);
        if (StringUtils.isBlank(snapshotOid)) {
            throw new ServiceException("System Task 表單快照寫入失敗");
        }
    }

    private String requiredVariable(DelegateExecution execution, String name)
            throws ServiceException {
        String value = Objects.toString(execution.getVariable(name), "");
        if (StringUtils.isBlank(value)) throw new ServiceException("缺少 Runtime 變數：" + name);
        return value;
    }

    private String requiredAttribute(ServiceTask task, String name)
            throws ServiceException {
        String value = attribute(task, name);
        if (StringUtils.isBlank(value)) throw new ServiceException("缺少 System Task 屬性：" + name);
        return value;
    }

    private String attribute(ServiceTask task, String name) {
        String value = task.getAttributeValue(
                FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE, name);
        if (StringUtils.isNotBlank(value)) return value;
        String fieldName = "flowmint" + Character.toUpperCase(name.charAt(0))
                + name.substring(1);
        return task.getFieldExtensions().stream()
                .filter(field -> fieldName.equals(field.getFieldName()))
                .map(org.flowable.bpmn.model.FieldExtension::getStringValue)
                .filter(StringUtils::isNotBlank)
                .findFirst().orElse(null);
    }
}
