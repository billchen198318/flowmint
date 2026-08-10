package org.qifu.fm.domain.runtime;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskPolicy;

import tools.jackson.databind.ObjectMapper;

public class FmApprovalGroupModeValidator {

    private final ObjectMapper objectMapper;

    public FmApprovalGroupModeValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validate(
            List<FmTaskPolicy> policies,
            List<FmTaskAssignmentRule> rules,
            Map<String, String> groupModes) throws ServiceException {
        Map<String, String> policyModes = policies.stream().collect(
                java.util.stream.Collectors.toMap(
                        FmTaskPolicy::getTaskDefKey,
                        FmTaskPolicy::getAssignmentMode));
        for (FmTaskAssignmentRule rule : rules) {
            if (!"ACTIVE".equals(rule.getStatus())
                    || !"APPROVAL_GROUP".equals(rule.getResolverType())) {
                continue;
            }
            String groupId = approvalGroupId(rule);
            String groupMode = groupModes.get(groupId);
            if (StringUtils.isBlank(groupMode)) {
                throw new ServiceException("簽核群組不存在或未啟用：" + groupId);
            }
            String policyMode = policyModes.get(rule.getTaskDefKey());
            if (!groupMode.equals(policyMode)) {
                throw new ServiceException("User Task " + rule.getTaskDefKey()
                        + " 的派送方式必須與簽核群組一致（" + groupMode + "）");
            }
        }
    }

    private String approvalGroupId(FmTaskAssignmentRule rule) throws ServiceException {
        try {
            String value = objectMapper.readTree(StringUtils.defaultIfBlank(
                    rule.getResolverConfig(), "{}"))
                    .path("approvalGroupId").asString();
            if (StringUtils.isBlank(value)) {
                throw new ServiceException("簽核群組規則缺少 approvalGroupId");
            }
            return value;
        } catch (ServiceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ServiceException("簽核群組規則設定不是有效 JSON");
        }
    }
}
