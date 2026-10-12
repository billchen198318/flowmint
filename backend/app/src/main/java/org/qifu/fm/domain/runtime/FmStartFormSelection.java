package org.qifu.fm.domain.runtime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmTaskFormRule;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Selects only the forms explicitly designated for process start in this version. */
public final class FmStartFormSelection {

    private FmStartFormSelection() {
    }

    public static List<FmTaskFormRule> select(List<FmTaskFormRule> rules,
            ObjectMapper objectMapper) throws ServiceException {
        Map<String, FmTaskFormRule> all = new LinkedHashMap<>();
        Map<String, FmTaskFormRule> selected = new LinkedHashMap<>();
        for (FmTaskFormRule rule : rules) {
            String key = rule.getFormId() + ":" + rule.getFormVersionNo();
            all.putIfAbsent(key, rule);
            if (startForm(rule.getFieldPolicy(), objectMapper)) {
                selected.putIfAbsent(key, rule);
            }
        }
        if (all.isEmpty()) {
            throw new ServiceException("流程版本沒有綁定表單");
        }
        if (selected.isEmpty() && all.size() == 1) {
            return List.copyOf(all.values());
        }
        if (selected.isEmpty()) {
            throw new ServiceException("流程版本綁定多張表單，須明確指定起單用表單");
        }
        return List.copyOf(selected.values());
    }

    public static boolean startForm(String fieldPolicy, ObjectMapper objectMapper)
            throws ServiceException {
        try {
            JsonNode policy = objectMapper.readTree(fieldPolicy);
            JsonNode value = policy.path("startForm");
            if (!value.isMissingNode() && !value.isBoolean()) {
                throw new ServiceException("起單用表單設定須為布林值");
            }
            return value.asBoolean(false);
        } catch (ServiceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ServiceException("起單用表單設定無法解析");
        }
    }
}
