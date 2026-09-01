package org.qifu.fm.domain.runtime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.logic.IFmDataActionLogicService;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class FmTaskActionHookExecutor {

    public static final String BEFORE_FORM_UPDATE = "BEFORE_FORM_UPDATE";
    public static final String AFTER_PROCESS_COMPLETE = "AFTER_PROCESS_COMPLETE";

    private final IFmDataActionLogicService dataActionLogicService;
    private final ObjectMapper objectMapper;

    public FmTaskActionHookExecutor(
            IFmDataActionLogicService dataActionLogicService,
            ObjectMapper objectMapper) {
        this.dataActionLogicService = dataActionLogicService;
        this.objectMapper = objectMapper;
    }

    public void execute(
            String uiSchemaContent,
            String phase,
            String tenantId,
            String taskDefKey,
            String actionType,
            String loginAccount,
            Map<String, Object> formData,
            Map<String, Object> context) throws ServiceException {
        JsonNode hooks;
        try {
            hooks = objectMapper.readTree(StringUtils.defaultIfBlank(
                    uiSchemaContent, "{}")).path("taskActionHooks");
        } catch (RuntimeException exception) {
            throw new ServiceException("Task Action Hook 設定格式錯誤");
        }
        if (!hooks.isArray()) {
            return;
        }
        for (JsonNode hook : hooks) {
            if (!phase.equals(hook.path("phase").asString())
                    || !matches(hook.path("taskDefKey").asString(), taskDefKey)
                    || !matchesAction(hook.path("actionTypes"), actionType)) {
                continue;
            }
            String hookId = hook.path("hookId").asString("");
            String actionCode = hook.path("actionCode").asString("");
            int versionNo = hook.path("actionVersion").asInt(0);
            if (StringUtils.isAnyBlank(hookId, actionCode) || versionNo <= 0) {
                throw new ServiceException("Task Action Hook 缺少識別、Action 或版本");
            }
            Map<String, Object> request = mappedRequest(
                    hook.path("requestMapping"), formData, context);
            FmDataActionExecutionView execution = dataActionLogicService.execute(
                    tenantId, actionCode, versionNo, request, loginAccount).getValue();
            Map<String, Object> response = execution.data();
            assertSuccess(hook, response);
            applyResponse(hook.path("responseMapping"), response, formData);
        }
    }

    private boolean matches(String configured, String actual) {
        return "*".equals(configured) || configured.equals(actual);
    }

    private boolean matchesAction(JsonNode actionTypes, String actionType) {
        if (!actionTypes.isArray()) {
            return false;
        }
        for (JsonNode value : actionTypes) {
            if (actionType.equals(value.asString())) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> mappedRequest(
            JsonNode mapping,
            Map<String, Object> formData,
            Map<String, Object> context) throws ServiceException {
        if (!mapping.isObject()) {
            throw new ServiceException("Task Action Hook requestMapping 必須是物件");
        }
        Map<String, Object> request = new LinkedHashMap<>();
        mapping.properties().forEach(entry -> {
            String source = entry.getValue().asString();
            Object value = source.startsWith("json:form.")
                    ? objectMapper.writeValueAsString(
                            path(formData, source.substring(10)))
                    : source.startsWith("form.")
                    ? path(formData, source.substring(5))
                    : source.startsWith("context.")
                            ? path(context, source.substring(8)) : null;
            request.put(entry.getKey(), value);
        });
        return request;
    }

    private void assertSuccess(JsonNode hook, Map<String, Object> response)
            throws ServiceException {
        String successPath = hook.path("successPath").asString("");
        if (successPath.isBlank()) {
            return;
        }
        Object value = path(response, successPath);
        boolean success = Boolean.TRUE.equals(value)
                || value instanceof Number number && number.intValue() == 1
                || "true".equalsIgnoreCase(String.valueOf(value))
                || "1".equals(String.valueOf(value));
        if (success) {
            return;
        }
        Object message = path(response, hook.path("messagePath").asString(""));
        throw new ServiceException(StringUtils.defaultIfBlank(
                message == null ? null : String.valueOf(message),
                "Task Action Hook 驗證失敗"));
    }

    private void applyResponse(
            JsonNode mapping,
            Map<String, Object> response,
            Map<String, Object> formData) throws ServiceException {
        if (mapping.isMissingNode() || mapping.isNull()) {
            return;
        }
        if (!mapping.isObject()) {
            throw new ServiceException("Task Action Hook responseMapping 必須是物件");
        }
        mapping.properties().forEach(entry -> {
            String target = entry.getValue().asString();
            if (target.startsWith("form.")) {
                setPath(formData, target.substring(5), path(response, entry.getKey()));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Object path(Map<String, Object> source, String path) {
        if (source == null || path == null || path.isBlank()) {
            return null;
        }
        Object current = source;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private void setPath(Map<String, Object> target, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = target;
        for (int index = 0; index < parts.length - 1; index++) {
            Object child = current.get(parts[index]);
            if (!(child instanceof Map<?, ?>)) {
                child = new LinkedHashMap<String, Object>();
                current.put(parts[index], child);
            }
            current = (Map<String, Object>) child;
        }
        current.put(parts[parts.length - 1], value);
    }
}
