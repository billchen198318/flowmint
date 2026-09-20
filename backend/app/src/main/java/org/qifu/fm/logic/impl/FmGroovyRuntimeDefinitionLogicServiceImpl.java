package org.qifu.fm.logic.impl;

import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyRuntimePreparation;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.logic.IFmGroovyRuntimeDefinitionLogicService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessGroovyManifestService;
import org.qifu.fm.service.IFmProcessSystemTaskService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Loads only fixed versions; never falls back to a main record's current version. */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class FmGroovyRuntimeDefinitionLogicServiceImpl implements IFmGroovyRuntimeDefinitionLogicService {

    private final IFmProcessDefService definitions;
    private final IFmProcessVersionService versions;
    private final IFmProcessGroovyManifestService manifests;
    private final IFmProcessSystemTaskService bindings;
    private final IFmTaskFormRuleService formRules;
    private final IFmFormVersionService forms;

    public FmGroovyRuntimeDefinitionLogicServiceImpl(IFmProcessDefService definitions,
            IFmProcessVersionService versions, IFmProcessGroovyManifestService manifests,
            IFmProcessSystemTaskService bindings, IFmTaskFormRuleService formRules,
            IFmFormVersionService forms) {
        this.definitions = definitions;
        this.versions = versions;
        this.manifests = manifests;
        this.bindings = bindings;
        this.formRules = formRules;
        this.forms = forms;
    }

    @Override
    public FmGroovyRuntimePreparation load(String tenantId, String processDefId, int versionNo,
            String nodeId, String formId, int formVersionNo,
            FmGroovyVersionManifest.EngineProfile trustedProfile) throws ServiceException {
        require(tenantId != null && !tenantId.isBlank() && processDefId != null && !processDefId.isBlank()
                && nodeId != null && !nodeId.isBlank() && formId != null && !formId.isBlank()
                && versionNo > 0 && formVersionNo > 0 && trustedProfile != null);
        Map<String, Object> identity = Map.of("tenantId", tenantId, "processDefId", processDefId,
                "versionNo", versionNo);
        var definition = one(definitions.selectListByParams(
                Map.of("tenantId", tenantId, "processDefId", processDefId)).getValue());
        require(tenantId.equals(definition.getTenantId()) && processDefId.equals(definition.getProcessDefId()));
        var version = one(versions.selectListByParams(identity).getValue());
        require(tenantId.equals(version.getTenantId()) && processDefId.equals(version.getProcessDefId())
                && Integer.valueOf(versionNo).equals(version.getVersionNo()) && released(version.getVersionStatus()));
        var manifest = one(manifests.selectListByParams(identity).getValue());
        require(tenantId.equals(manifest.getTenantId()) && processDefId.equals(manifest.getProcessDefId())
                && Integer.valueOf(versionNo).equals(manifest.getVersionNo())
                && trustedProfile.canonical().equals(manifest.getEngineProfile())
                && version.getBpmnSha256() != null && version.getBpmnSha256().equals(manifest.getBpmnSha256()));

        // Existing start-form selection is derived from Task Form Rules. Require one
        // unambiguous form/version across those rules, matching the persisted form data.
        var rules = formRules.findByVersion(tenantId, processDefId, versionNo);
        require(rules != null && !rules.isEmpty());
        for (var rule : rules) {
            require(rule != null && tenantId.equals(rule.getTenantId())
                    && processDefId.equals(rule.getProcessDefId())
                    && Integer.valueOf(versionNo).equals(rule.getProcessVersionNo())
                    && formId.equals(rule.getFormId()) && Integer.valueOf(formVersionNo).equals(rule.getFormVersionNo()));
        }
        var form = one(forms.selectListByParams(Map.of("tenantId", tenantId,
                "formId", formId, "versionNo", formVersionNo)).getValue());
        require(tenantId.equals(form.getTenantId()) && formId.equals(form.getFormId())
                && Integer.valueOf(formVersionNo).equals(form.getVersionNo()) && released(form.getVersionStatus()));
        var storedBindings = bindings.findVersion(tenantId, processDefId, versionNo);
        require(storedBindings != null && !storedBindings.isEmpty());
        for (var binding : storedBindings) {
            require(binding != null && tenantId.equals(binding.getTenantId())
                    && processDefId.equals(binding.getProcessDefId())
                    && Integer.valueOf(versionNo).equals(binding.getVersionNo())
                    && "GROOVY".equals(binding.getTaskType()));
        }
        var commands = storedBindings.stream().map(binding -> new FmGroovyBindingCommand(
                binding.getNodeId(), binding.getBindingId(), binding.getScriptContent(), binding.getInputSchema(),
                binding.getOutputSchema(), binding.getMappingContent(), binding.getTimeoutMs())).toList();
        try {
            return new FmGroovyRuntimePreparation(version, definition.getProcessKey(), commands, trustedProfile,
                    manifest.getManifestContent(), manifest.getManifestSha256(), nodeId, form.getSchemaContent());
        } catch (IllegalArgumentException invalid) {
            throw new ServiceException("GROOVY_RUNTIME_DEFINITION_INVALID");
        }
    }

    private static boolean released(String status) {
        return "PUBLISHED".equals(status) || "RETIRED".equals(status);
    }

    private static <T> T one(List<T> values) throws ServiceException {
        require(values != null && values.size() == 1 && values.getFirst() != null);
        return values.getFirst();
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_RUNTIME_DEFINITION_INVALID");
        }
    }
}
