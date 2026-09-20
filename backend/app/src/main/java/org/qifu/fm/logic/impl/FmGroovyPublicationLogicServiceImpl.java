package org.qifu.fm.logic.impl;

import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyContractJson;
import org.qifu.fm.domain.workflow.FmGroovyPublicationCompiler.VerifiedPublication;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessGroovyManifest;
import org.qifu.fm.logic.IFmGroovyPublicationLogicService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessGroovyManifestService;
import org.qifu.fm.service.IFmProcessSystemTaskService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
public class FmGroovyPublicationLogicServiceImpl implements IFmGroovyPublicationLogicService {

    private final IFmProcessVersionService versions;
    private final IFmProcessDefService definitions;
    private final IFmProcessSystemTaskService bindings;
    private final IFmTaskFormRuleService rules;
    private final IFmFormVersionService forms;
    private final IFmProcessGroovyManifestService manifests;

    public FmGroovyPublicationLogicServiceImpl(IFmProcessVersionService versions,
            IFmProcessDefService definitions, IFmProcessSystemTaskService bindings,
            IFmTaskFormRuleService rules, IFmFormVersionService forms,
            IFmProcessGroovyManifestService manifests) {
        this.versions = versions;
        this.definitions = definitions;
        this.bindings = bindings;
        this.rules = rules;
        this.forms = forms;
        this.manifests = manifests;
    }

    @Override
    public void persist(String tenantId, String versionOid, int expectedLockVersion,
            VerifiedPublication evidence) throws ServiceException {
        require(tenantId != null && !tenantId.isBlank() && versionOid != null && !versionOid.isBlank()
                && expectedLockVersion >= 0 && evidence != null);
        require(Integer.valueOf(expectedLockVersion).equals(versions.lockDraft(tenantId, versionOid)));
        var version = versions.selectByPrimaryKey(versionOid).getValueEmptyThrowMessage();
        require(tenantId.equals(version.getTenantId()) && "DRAFT".equals(version.getVersionStatus()));
        var definition = one(definitions.selectListByParams(Map.of("tenantId", tenantId,
                "processDefId", version.getProcessDefId())).getValue());
        require(tenantId.equals(definition.getTenantId())
                && version.getProcessDefId().equals(definition.getProcessDefId()));
        var stored = bindings.findVersion(tenantId, version.getProcessDefId(), version.getVersionNo());
        require(stored != null && !stored.isEmpty());
        for (var binding : stored) {
            require(binding != null && tenantId.equals(binding.getTenantId())
                    && version.getProcessDefId().equals(binding.getProcessDefId())
                    && version.getVersionNo().equals(binding.getVersionNo()) && "GROOVY".equals(binding.getTaskType()));
        }
        var commands = stored.stream().map(binding -> new FmGroovyBindingCommand(binding.getNodeId(),
                binding.getBindingId(), binding.getScriptContent(), binding.getInputSchema(),
                binding.getOutputSchema(), binding.getMappingContent(), binding.getTimeoutMs())).toList();
        var current = FmGroovyVersionManifest.build(version, definition.getProcessKey(), commands, evidence.profile());
        require(current.equals(evidence.manifest()));
        var formRules = rules.findByVersion(tenantId, version.getProcessDefId(), version.getVersionNo());
        require(formRules != null && !formRules.isEmpty() && formRules.getFirst() != null);
        var first = formRules.getFirst();
        require(first.getFormId() != null && first.getFormVersionNo() != null);
        for (var rule : formRules) {
            require(rule != null && tenantId.equals(rule.getTenantId())
                    && version.getProcessDefId().equals(rule.getProcessDefId())
                    && version.getVersionNo().equals(rule.getProcessVersionNo())
                    && first.getFormId().equals(rule.getFormId())
                    && first.getFormVersionNo().equals(rule.getFormVersionNo()));
        }
        var form = one(forms.selectListByParams(Map.of("tenantId", tenantId, "formId", first.getFormId(),
                "versionNo", first.getFormVersionNo())).getValue());
        require(tenantId.equals(form.getTenantId()) && first.getFormId().equals(form.getFormId())
                && first.getFormVersionNo().equals(form.getVersionNo()) && "PUBLISHED".equals(form.getVersionStatus())
                && form.getSchemaContent() != null
                && FmGroovyContractJson.sha256(form.getSchemaContent()).equals(evidence.formSchemaSha256()));
        var value = new FmProcessGroovyManifest();
        value.setTenantId(tenantId);
        value.setProcessDefId(version.getProcessDefId());
        value.setVersionNo(version.getVersionNo());
        value.setManifestContent(current.canonicalJson());
        value.setManifestSha256(current.sha256());
        value.setBpmnSha256(current.bpmnSha256());
        value.setEngineProfile(evidence.profile().canonical());
        manifests.insert(value).getValueEmptyThrowMessage();
        // The enclosing publisher must deploy and mark the version PUBLISHED in this
        // same transaction. A later failure must roll this insert back as well.
    }

    private static <T> T one(List<T> values) throws ServiceException {
        require(values != null && values.size() == 1 && values.getFirst() != null);
        return values.getFirst();
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_PUBLICATION_STALE_CONTENT");
        }
    }
}
