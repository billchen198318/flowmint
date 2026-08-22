package org.qifu.fm.domain.workflow;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.entity.FmTaskPolicy;

/** Validates that persisted task configuration matches the BPMN user tasks exactly. */
public class FmProcessPublishValidator {

    public void validate(
            Set<String> taskKeys,
            Collection<FmTaskFormRule> formRules,
            Collection<FmTaskPolicy> policies,
            Collection<FmTaskAssignmentRule> assignmentRules) throws ServiceException {
        requireExactlyOne(taskKeys, formRules, FmTaskFormRule::getTaskDefKey, "表單規則");
        requireExactlyOne(taskKeys, policies, FmTaskPolicy::getTaskDefKey, "Task Policy");
        rejectUnknownTaskKeys(taskKeys, assignmentRules,
                FmTaskAssignmentRule::getTaskDefKey, "指派規則");

        Map<String, FmTaskPolicy> policyByTask = new HashMap<>();
        for (FmTaskPolicy policy : policies) {
            policyByTask.put(policy.getTaskDefKey(), policy);
        }
        Set<String> tasksWithActiveAssignment = new HashSet<>();
        for (FmTaskAssignmentRule rule : assignmentRules) {
            if ("ACTIVE".equals(rule.getStatus())) {
                tasksWithActiveAssignment.add(rule.getTaskDefKey());
            }
        }
        for (String taskKey : taskKeys) {
            FmTaskPolicy policy = policyByTask.get(taskKey);
            if (!"APPLICANT_CORRECTION".equals(policy.getAssignmentMode())
                    && !tasksWithActiveAssignment.contains(taskKey)) {
                throw new ServiceException(
                        "User Task「" + taskKey + "」至少需要一筆啟用中的指派規則");
            }
        }

        boolean allowsReturn = policies.stream()
                .anyMatch(policy -> "Y".equals(policy.getAllowReturn()));
        boolean hasCorrectionTask = policies.stream().anyMatch(policy ->
                "APPLICANT_CORRECTION".equals(policy.getAssignmentMode()));
        if (allowsReturn && !hasCorrectionTask) {
            throw new ServiceException(
                    "流程允許退回時，必須有一個 APPLICANT_CORRECTION 補件任務");
        }
    }

    private <T> void requireExactlyOne(
            Set<String> taskKeys,
            Collection<T> values,
            Function<T, String> keyExtractor,
            String configurationName) throws ServiceException {
        rejectUnknownTaskKeys(taskKeys, values, keyExtractor, configurationName);
        Map<String, Integer> counts = new HashMap<>();
        for (T value : values) {
            counts.merge(keyExtractor.apply(value), 1, Integer::sum);
        }
        for (String taskKey : taskKeys) {
            int count = counts.getOrDefault(taskKey, 0);
            if (count != 1) {
                throw new ServiceException("User Task「" + taskKey + "」必須恰有一筆"
                        + configurationName + "，目前為 " + count + " 筆");
            }
        }
    }

    private <T> void rejectUnknownTaskKeys(
            Set<String> taskKeys,
            Collection<T> values,
            Function<T, String> keyExtractor,
            String configurationName) throws ServiceException {
        Set<String> unknown = new java.util.TreeSet<>();
        for (T value : values) {
            String taskKey = keyExtractor.apply(value);
            if (!taskKeys.contains(taskKey)) {
                unknown.add(String.valueOf(taskKey));
            }
        }
        if (!unknown.isEmpty()) {
            throw new ServiceException(configurationName + "包含 BPMN 不存在的 User Task："
                    + String.join(", ", unknown));
        }
    }
}
