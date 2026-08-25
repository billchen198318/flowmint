package org.qifu.fm.logic.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.domain.authority.FmApprovalAuthorityConditionEvaluator;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmApprovalAuthorityCommand;
import org.qifu.fm.dto.command.FmApprovalAuthorityRuleCommand;
import org.qifu.fm.dto.view.FmApprovalAuthorityRuleView;
import org.qifu.fm.dto.view.FmApprovalAuthorityView;
import org.qifu.fm.entity.FmApprovalAuthority;
import org.qifu.fm.entity.FmApprovalAuthorityRule;
import org.qifu.fm.logic.IFmApprovalAuthorityLogicService;
import org.qifu.fm.service.IFmApprovalAuthorityRuleService;
import org.qifu.fm.service.IFmApprovalAuthorityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmApprovalAuthorityLogicServiceImpl
        implements IFmApprovalAuthorityLogicService {

    private static final List<String> STATUSES = List.of("ACTIVE", "INACTIVE");
    private static final List<String> TARGET_TYPES = List.of(
            "APPROVAL_LEVEL",
            "ORG_TITLE",
            "ORG_DUTY",
            "APPROVAL_GROUP",
            "FIXED_ACCOUNT");

    private final IFmApprovalAuthorityService authorityService;
    private final IFmApprovalAuthorityRuleService ruleService;
    private final FmApprovalAuthorityConditionEvaluator conditionEvaluator;
    private final FmTenantAccessGuard tenantAccessGuard;
    private final ObjectMapper objectMapper;

    public FmApprovalAuthorityLogicServiceImpl(
            IFmApprovalAuthorityService authorityService,
            IFmApprovalAuthorityRuleService ruleService,
            FmApprovalAuthorityConditionEvaluator conditionEvaluator,
            FmTenantAccessGuard tenantAccessGuard,
            ObjectMapper objectMapper) {
        this.authorityService = authorityService;
        this.ruleService = ruleService;
        this.conditionEvaluator = conditionEvaluator;
        this.tenantAccessGuard = tenantAccessGuard;
        this.objectMapper = objectMapper;
    }

    @Override
    public DefaultResult<List<FmApprovalAuthorityView>> findByProcess(
            String tenantId,
            String processDefId) throws ServiceException {
        if (StringUtils.isAnyBlank(tenantId, processDefId)) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        tenantAccessGuard.requireAccess(tenantId);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processDefId", processDefId);
        return success(authorityService
                .selectListByParams(parameters, "AUTHORITY_CODE", "ASC")
                .getValue().stream()
                .map(this::view)
                .toList());
    }

    @Override
    public DefaultResult<FmApprovalAuthorityView> load(String oid) throws ServiceException {
        FmApprovalAuthority authority = requiredAuthority(oid);
        tenantAccessGuard.requireAccess(authority.getTenantId());
        return success(view(authority));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmApprovalAuthorityView> create(FmApprovalAuthorityCommand command)
            throws ServiceException {
        validate(command, true);
        tenantAccessGuard.requireAccess(command.tenantId());
        FmApprovalAuthority authority = new FmApprovalAuthority();
        authority.setTenantId(command.tenantId());
        authority.setApprovalAuthorityId(UUID.randomUUID().toString());
        authority.setAuthorityCode(command.authorityCode());
        apply(authority, command);
        ensureUniqueCode(authority, null);
        authorityService.insert(authority);
        replaceRules(authority, command.rules());
        return success(view(authority));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmApprovalAuthorityView> update(FmApprovalAuthorityCommand command)
            throws ServiceException {
        validate(command, false);
        FmApprovalAuthority authority = requiredAuthority(command.oid());
        tenantAccessGuard.requireAccess(authority.getTenantId());
        if (!authority.getTenantId().equals(command.tenantId())) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        authority.setAuthorityCode(command.authorityCode());
        apply(authority, command);
        ensureUniqueCode(authority, authority.getOid());
        authorityService.update(authority);
        replaceRules(authority, command.rules());
        return success(view(authority));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmApprovalAuthorityView> deactivate(String oid)
            throws ServiceException {
        FmApprovalAuthority authority = requiredAuthority(oid);
        tenantAccessGuard.requireAccess(authority.getTenantId());
        authority.setStatus("INACTIVE");
        authorityService.update(authority);
        return success(view(authority));
    }

    private void validate(FmApprovalAuthorityCommand command, boolean create)
            throws ServiceException {
        if (command == null
                || !create && StringUtils.isBlank(command.oid())
                || StringUtils.isAnyBlank(
                        command.tenantId(),
                        command.authorityCode(),
                        command.authorityName(),
                        command.processDefId())
                || command.effectiveFrom() == null
                || command.effectiveTo() != null
                        && !command.effectiveTo().after(command.effectiveFrom())
                || !STATUSES.contains(StringUtils.defaultIfBlank(command.status(), "ACTIVE"))
                || command.rules() == null
                || command.rules().isEmpty()) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        for (FmApprovalAuthorityRuleCommand rule : command.rules()) {
            validateRule(rule);
        }
        long distinctSequences = command.rules().stream()
                .map(FmApprovalAuthorityRuleCommand::ruleSeq)
                .distinct()
                .count();
        if (distinctSequences != command.rules().size()) {
            throw new ServiceException("核決權限規則順序不可重複");
        }
    }

    private void validateRule(FmApprovalAuthorityRuleCommand rule) throws ServiceException {
        if (rule == null
                || rule.ruleSeq() == null
                || rule.ruleSeq() < 1
                || StringUtils.isBlank(rule.conditionConfig())
                || !TARGET_TYPES.contains(rule.targetType())
                || StringUtils.isBlank(rule.targetRefId())
                || !List.of("Y", "N").contains(
                        StringUtils.defaultIfBlank(rule.stopAfterApproval(), "N"))
                || !STATUSES.contains(StringUtils.defaultIfBlank(rule.status(), "ACTIVE"))) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        conditionEvaluator.validate(rule.conditionConfig());
        validateLevelMatchMode(rule);
    }

    private void validateLevelMatchMode(FmApprovalAuthorityRuleCommand rule)
            throws ServiceException {
        if (!"APPROVAL_LEVEL".equals(rule.targetType())) {
            return;
        }
        try {
            JsonNode config = objectMapper.readTree(
                    StringUtils.defaultIfBlank(rule.resolverConfig(), "{}"));
            String mode = config.path("levelMatchMode").asString("EXACT");
            if (!config.isObject()
                    || !List.of("EXACT", "EXACT_OR_HIGHER", "UP_TO_LEVEL").contains(mode)) {
                throw new ServiceException("簽核層級匹配模式不正確");
            }
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("核決權限 Resolver Config JSON 格式錯誤");
        }
    }

    private void apply(
            FmApprovalAuthority authority,
            FmApprovalAuthorityCommand command) {
        authority.setAuthorityName(command.authorityName());
        authority.setProcessDefId(command.processDefId());
        authority.setFormId(command.formId());
        authority.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
        authority.setEffectiveFrom(command.effectiveFrom());
        authority.setEffectiveTo(command.effectiveTo());
        authority.setDescription(command.description());
    }

    private void ensureUniqueCode(FmApprovalAuthority authority, String ignoredOid)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", authority.getTenantId());
        parameters.put("authorityCode", authority.getAuthorityCode());
        boolean duplicate = authorityService.selectListByParams(parameters, null, null)
                .getValue().stream()
                .anyMatch(value -> !value.getOid().equals(ignoredOid));
        if (duplicate) {
            throw new ServiceException("核決權限代碼已存在");
        }
    }

    private void replaceRules(
            FmApprovalAuthority authority,
            List<FmApprovalAuthorityRuleCommand> commands) {
        List<FmApprovalAuthorityRule> rules = commands.stream()
                .map(command -> rule(authority, command))
                .toList();
        ruleService.replaceAuthority(
                authority.getTenantId(),
                authority.getApprovalAuthorityId(),
                rules);
    }

    private FmApprovalAuthorityRule rule(
            FmApprovalAuthority authority,
            FmApprovalAuthorityRuleCommand command) {
        FmApprovalAuthorityRule rule = new FmApprovalAuthorityRule();
        rule.setTenantId(authority.getTenantId());
        rule.setApprovalAuthorityRuleId(UUID.randomUUID().toString());
        rule.setApprovalAuthorityId(authority.getApprovalAuthorityId());
        rule.setRuleSeq(command.ruleSeq());
        rule.setConditionConfig(command.conditionConfig());
        rule.setTargetType(command.targetType());
        rule.setTargetRefId(command.targetRefId());
        rule.setResolverConfig(command.resolverConfig());
        rule.setStopAfterApproval(
                StringUtils.defaultIfBlank(command.stopAfterApproval(), "N"));
        rule.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
        return rule;
    }

    private FmApprovalAuthority requiredAuthority(String oid) throws ServiceException {
        if (StringUtils.isBlank(oid)) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        return authorityService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
    }

    private FmApprovalAuthorityView view(FmApprovalAuthority authority) {
        List<FmApprovalAuthorityRuleView> rules = ruleService.findByAuthority(
                authority.getTenantId(),
                authority.getApprovalAuthorityId()).stream()
                .map(value -> new FmApprovalAuthorityRuleView(
                        value.getOid(),
                        value.getApprovalAuthorityRuleId(),
                        value.getRuleSeq(),
                        value.getConditionConfig(),
                        value.getTargetType(),
                        value.getTargetRefId(),
                        value.getTargetRefId(),
                        value.getResolverConfig(),
                        value.getStopAfterApproval(),
                        value.getStatus()))
                .toList();
        return new FmApprovalAuthorityView(
                authority.getOid(),
                authority.getTenantId(),
                authority.getApprovalAuthorityId(),
                authority.getAuthorityCode(),
                authority.getAuthorityName(),
                authority.getProcessDefId(),
                authority.getFormId(),
                authority.getStatus(),
                authority.getEffectiveFrom(),
                authority.getEffectiveTo(),
                authority.getDescription(),
                rules);
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
