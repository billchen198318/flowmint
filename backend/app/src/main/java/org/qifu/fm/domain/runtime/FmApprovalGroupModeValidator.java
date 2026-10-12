package org.qifu.fm.domain.runtime;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskPolicy;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

public class FmApprovalGroupModeValidator {

    private final ObjectMapper objectMapper;

    public FmApprovalGroupModeValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validate(
            List<FmTaskPolicy> policies,
            List<FmTaskAssignmentRule> rules,
            Map<String, String> groupModes) throws ServiceException {
        validate(policies, rules, groupModes, Map.of());
    }

    public void validate(
            List<FmTaskPolicy> policies,
            List<FmTaskAssignmentRule> rules,
            Map<String, String> groupModes,
            Map<String, Set<String>> authorityGroupIds) throws ServiceException {
        Map<String, String> policyModes = policies.stream().collect(
                java.util.stream.Collectors.toMap(
                        FmTaskPolicy::getTaskDefKey,
                        FmTaskPolicy::getAssignmentMode));
        for (FmTaskAssignmentRule rule : rules) {
            if (!"ACTIVE".equals(rule.getStatus())) {
                continue;
            }
            validateResolver(rule, policyModes.get(rule.getTaskDefKey()),
                    rule.getResolverType(), rule.getResolverConfig(),
                    groupModes, authorityGroupIds);
            if (StringUtils.isNotBlank(rule.getFallbackConfig())) {
                try {
                    JsonNode fallback = objectMapper.readTree(rule.getFallbackConfig());
                    validateResolver(rule, policyModes.get(rule.getTaskDefKey()),
                            fallback.path("resolverType").asString(),
                            objectMapper.writeValueAsString(fallback.path("resolverConfig")),
                            groupModes, authorityGroupIds);
                } catch (ServiceException exception) {
                    throw exception;
                } catch (RuntimeException exception) {
                    throw new ServiceException("Fallback Resolver 設定不是有效 JSON");
                }
            }
        }
    }

    private void validateResolver(FmTaskAssignmentRule rule, String policyMode,
            String resolverType, String resolverConfig,
            Map<String, String> groupModes,
            Map<String, Set<String>> authorityGroupIds) throws ServiceException {
        if (!Set.of("APPROVAL_GROUP", "APPROVAL_AUTHORITY").contains(resolverType)) {
            return;
        }
        JsonNode config;
        try {
            config = objectMapper.readTree(StringUtils.defaultIfBlank(resolverConfig, "{}"));
        } catch (RuntimeException exception) {
            throw new ServiceException("簽核群組規則設定不是有效 JSON");
        }
        Set<String> groupIds = "APPROVAL_GROUP".equals(resolverType)
                ? Set.of(config.path("approvalGroupId").asString())
                : authorityGroupIds.getOrDefault(
                        config.path("approvalAuthorityId").asString(), Set.of());
        for (String groupId : groupIds) {
            String groupMode = groupModes.get(groupId);
            if (StringUtils.isBlank(groupMode)) {
                throw new ServiceException("簽核群組不存在或未啟用：" + groupId);
            }
            if (!groupMode.equals(policyMode)) {
                throw new ServiceException("User Task " + rule.getTaskDefKey()
                        + " 的派送方式必須與簽核群組一致（" + groupMode + "）");
            }
        }
    }
}
