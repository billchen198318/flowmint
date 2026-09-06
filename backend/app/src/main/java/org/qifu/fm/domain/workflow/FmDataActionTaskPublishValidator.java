package org.qifu.fm.domain.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ServiceTask;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.entity.FmDataActionVersion;
import org.qifu.fm.entity.FmDataActionStep;
import org.qifu.fm.service.IFmDataActionService;
import org.qifu.fm.service.IFmDataActionStepService;
import org.qifu.fm.service.IFmDataActionVersionService;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class FmDataActionTaskPublishValidator {

    public static final String FLOWMINT_NAMESPACE =
            "https://flowmint.qifu.org/schema/bpmn";

    private final IFmDataActionService actionService;
    private final IFmDataActionVersionService versionService;
    private final IFmDataActionStepService stepService;
    private final ObjectMapper objectMapper;

    public FmDataActionTaskPublishValidator(
            IFmDataActionService actionService,
            IFmDataActionVersionService versionService,
            IFmDataActionStepService stepService,
            ObjectMapper objectMapper) {
        this.actionService = actionService;
        this.versionService = versionService;
        this.stepService = stepService;
        this.objectMapper = objectMapper;
    }

    public void validate(String tenantId, BpmnModel model) throws ServiceException {
        for (ServiceTask task : model.getMainProcess()
                .findFlowElementsOfType(ServiceTask.class)) {
            validate(tenantId, task);
        }
    }

    private void validate(String tenantId, ServiceTask task) throws ServiceException {
        String actionCode = task.getAttributeValue(FLOWMINT_NAMESPACE, "actionCode");
        int actionVersion = integerAttribute(task, "actionVersion");
        Map<String, Object> actionParams = new HashMap<>();
        actionParams.put("tenantId", tenantId);
        actionParams.put("actionCode", actionCode);
        actionParams.put("status", "ACTIVE");
        List<FmDataAction> actions = Objects.requireNonNullElse(
                actionService.selectListByParams(actionParams, "ACTION_CODE", "ASC")
                        .getValue(),
                List.of());
        FmDataAction action = actions.stream().findFirst().orElse(null);
        if (action == null) {
            throw new ServiceException("Data Action Task「" + label(task)
                    + "」引用了不存在或已停用的 Action " + actionCode);
        }
        if (!"QUERY".equals(action.getActionType())) {
            // Mutation actions must never be repeated automatically. A failed job is
            // retained by Flowable for explicit operator review/retry.
            task.setFailedJobRetryTimeCycleValue("R0/PT1M");
        }
        Map<String, Object> versionParams = new HashMap<>();
        versionParams.put("tenantId", tenantId);
        versionParams.put("actionId", action.getActionId());
        versionParams.put("versionNo", actionVersion);
        versionParams.put("versionStatus", "PUBLISHED");
        List<FmDataActionVersion> versions = Objects.requireNonNullElse(
                versionService.selectListByParams(
                        versionParams, "VERSION_NO", "DESC").getValue(),
                List.of());
        if (versions.isEmpty()) {
            throw new ServiceException("Data Action Task「" + label(task)
                    + "」引用的 Action Version 尚未發布："
                    + actionCode + " v" + actionVersion);
        }
        validateMappings(task, action, actionVersion);
    }

    private void validateMappings(ServiceTask task, FmDataAction action,
            int actionVersion) throws ServiceException {
        try {
            Map<String, String> requestSchema = objectMapper.readValue(
                    StringUtils.defaultIfBlank(action.getRequestSchema(), "{}"),
                    new TypeReference<Map<String, String>>() { });
            Map<String, String> requestMapping = mapping(task, "requestMapping");
            if (!requestSchema.keySet().equals(requestMapping.keySet())) {
                throw new ServiceException("Data Action Task「" + label(task)
                        + "」Request Mapping 必須完整對應 Action Request Schema");
            }
            Map<String, Object> params = new HashMap<>();
            params.put("tenantId", action.getTenantId());
            params.put("actionId", action.getActionId());
            params.put("versionNo", actionVersion);
            Set<String> responseKeys = Objects.requireNonNullElse(
                    stepService.selectListByParams(params, "STEP_NO", "ASC").getValue(),
                    List.<FmDataActionStep>of()).stream()
                    .filter(step -> !"NONE".equals(step.getResultMode()))
                    .map(FmDataActionStep::getResultKey)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            for (String source : mapping(task, "responseMapping").keySet()) {
                String root = StringUtils.substringBefore(source, ".");
                if (!responseKeys.contains(root)) {
                    throw new ServiceException("Data Action Task「" + label(task)
                            + "」Response Mapping 引用了不存在的結果：" + source);
                }
            }
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("Data Action Task「" + label(task)
                    + "」無法驗證 Mapping：" + exception.getMessage());
        }
    }

    private Map<String, String> mapping(ServiceTask task, String name) throws Exception {
        return objectMapper.readValue(StringUtils.defaultIfBlank(
                        task.getAttributeValue(FLOWMINT_NAMESPACE, name), "{}"),
                new TypeReference<Map<String, String>>() { });
    }

    private int integerAttribute(ServiceTask task, String name) throws ServiceException {
        try {
            return Integer.parseInt(task.getAttributeValue(FLOWMINT_NAMESPACE, name));
        } catch (NumberFormatException exception) {
            throw new ServiceException("Data Action Task「" + label(task)
                    + "」缺少合法的 " + name);
        }
    }

    private String label(ServiceTask task) {
        return StringUtils.defaultIfBlank(task.getName(), task.getId());
    }
}
