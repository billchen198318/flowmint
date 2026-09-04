package org.qifu.fm.flowable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmDataActionTaskPublishValidator;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.logic.IFmDataActionLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component("fmDataActionTaskDelegate")
public class FmDataActionTaskDelegate implements JavaDelegate {

    private final IFmDataActionLogicService actionLogicService;
    private final IFmFormDataService formDataService;
    private final ObjectMapper objectMapper;

    public FmDataActionTaskDelegate(
            IFmDataActionLogicService actionLogicService,
            IFmFormDataService formDataService,
            ObjectMapper objectMapper) {
        this.actionLogicService = actionLogicService;
        this.formDataService = formDataService;
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
            applyResponse(task, formData, result.data());
            execution.setVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA, formData);
            persistFormData(tenantId, requiredVariable(execution,
                    FmTaskAssignmentListener.VARIABLE_FORM_DATA_ID), formData);
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

    private void applyResponse(ServiceTask task, Map<String, Object> formData,
            Map<String, Object> result) throws Exception {
        for (Map.Entry<String, String> entry
                : mapping(task, "responseMapping").entrySet()) {
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> mutableFormData(DelegateExecution execution) {
        Object value = execution.getVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA);
        if (value instanceof Map<?, ?> source) {
            return new HashMap<>((Map<String, Object>) source);
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
        int expectedLockVersion = Objects.requireNonNullElse(formData.getLockVersion(), 0);
        int updated = formDataService.updateDataContent(tenantId, formDataId,
                objectMapper.writeValueAsString(data), expectedLockVersion);
        if (updated != 1) {
            throw new ServiceException("System Task 更新表單資料時發生併發衝突");
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
