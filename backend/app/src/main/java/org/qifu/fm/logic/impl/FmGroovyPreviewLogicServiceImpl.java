package org.qifu.fm.logic.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.domain.workflow.FmGroovyContractJson;
import org.qifu.fm.domain.workflow.FmGroovyDesignAccess;
import org.qifu.fm.domain.workflow.FmGroovyDraftValidator;
import org.qifu.fm.domain.workflow.FmGroovyPreviewProtocol;
import org.qifu.fm.domain.workflow.FmGroovyPreviewQuota;
import org.qifu.fm.domain.workflow.FmGroovyPreviewRunner;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest;
import org.qifu.fm.dto.command.FmGroovyPreviewCommand;
import org.qifu.fm.dto.view.FmGroovyPreviewView;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.logic.IFmGroovyPreviewLogicService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class FmGroovyPreviewLogicServiceImpl implements IFmGroovyPreviewLogicService {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final IFmProcessVersionService versions;
    private final IFmProcessDefService definitions;
    private final FmGroovyDesignAccess access;
    private final FmGroovyPreviewRunner runner;
    private final FmGroovyPreviewQuota quota;
    private final boolean draftEnabled;

    public FmGroovyPreviewLogicServiceImpl(IFmProcessVersionService versions, IFmProcessDefService definitions,
            FmGroovyDesignAccess access, FmGroovyPreviewRunner runner, FmGroovyPreviewQuota quota,
            @Value("${flowmint.groovy.draft-enabled:false}") boolean draftEnabled) {
        this.versions = versions;
        this.definitions = definitions;
        this.access = access;
        this.runner = runner;
        this.quota = quota;
        this.draftEnabled = draftEnabled;
    }

    @Override
    public DefaultResult<FmGroovyPreviewView> check(FmGroovyPreviewCommand command) throws ServiceException {
        return execute(command, "CHECK");
    }

    @Override
    public DefaultResult<FmGroovyPreviewView> preview(FmGroovyPreviewCommand command) throws ServiceException {
        return execute(command, "RUN");
    }

    private DefaultResult<FmGroovyPreviewView> execute(FmGroovyPreviewCommand command, String operation)
            throws ServiceException {
        if (!draftEnabled) {
            throw new ServiceException("GROOVY_PREVIEW_UNAVAILABLE");
        }
        if (command == null || command.oid() == null || command.oid().isBlank()
                || command.expectedLockVersion() == null || command.expectedLockVersion() < 0
                || command.bpmnXml() == null || command.bpmnXml().length() > 2 * 1024 * 1024
                || command.groovyBindings() == null || command.groovyBindings().size() > 100) {
            throw new ServiceException("GROOVY_PREVIEW_REQUEST_INVALID");
        }
        long designBytes = command.bpmnXml().getBytes(StandardCharsets.UTF_8).length;
        for (var binding : command.groovyBindings()) {
            if (binding == null || binding.scriptContent() == null || binding.inputSchema() == null
                    || binding.outputSchema() == null || binding.mappingContent() == null) {
                throw new ServiceException("GROOVY_PREVIEW_REQUEST_INVALID");
            }
            for (String content : List.of(binding.scriptContent(), binding.inputSchema(),
                    binding.outputSchema(), binding.mappingContent())) {
                designBytes += content.getBytes(StandardCharsets.UTF_8).length;
            }
            if (designBytes > 8 * 1024 * 1024) {
                throw new ServiceException("GROOVY_PREVIEW_REQUEST_INVALID");
            }
        }
        FmProcessVersion version = current(command, null);
        var profile = runner.profile();
        try (var permit = quota.acquire(version.getTenantId())) {
            var definition = definitions.selectListByParams(Map.of("tenantId", version.getTenantId(),
                    "processDefId", version.getProcessDefId())).getValue().stream().findFirst()
                    .orElseThrow(() -> new ServiceException("GROOVY_PROCESS_NOT_FOUND"));
            // Use a detached identity holder: the unsaved XML is never written to the loaded Entity.
            FmProcessVersion design = new FmProcessVersion();
            design.setTenantId(version.getTenantId());
            design.setProcessDefId(version.getProcessDefId());
            design.setVersionNo(version.getVersionNo());
            design.setBpmnXml(command.bpmnXml());
            FmGroovyVersionManifest.Manifest manifest;
            try {
                manifest = FmGroovyVersionManifest.build(design, definition.getProcessKey(), command.groovyBindings(), profile);
            } catch (ServiceException invalidDesign) {
                throw new ServiceException("GROOVY_PREVIEW_DESIGN_INVALID");
            }
            var binding = new FmGroovyDraftValidator().validate(design, command.bpmnXml(), command.groovyBindings())
                    .stream().filter(value -> value.getNodeId().equals(command.nodeId())).findFirst()
                    .orElseThrow(() -> new ServiceException("GROOVY_NODE_NOT_FOUND"));
            String hash = manifest.bindings().stream().filter(value -> value.nodeId().equals(command.nodeId()))
                    .findFirst().orElseThrow().sha256();
            JsonNode input = "CHECK".equals(operation) ? JSON.createObjectNode()
                    : FmGroovyContractJson.object(command.sampleInput());
            JsonNode inputSchema = FmGroovyContractJson.object(binding.getInputSchema());
            if ("RUN".equals(operation)) {
                FmGroovyContractJson.validateValue(input, inputSchema);
            }
            String invocationId = UUID.randomUUID().toString();
            ObjectNode context = JSON.createObjectNode();
            context.put("tenantId", version.getTenantId());
            context.put("processVersionNo", version.getVersionNo());
            context.put("nodeId", command.nodeId());
            context.put("invocationId", invocationId);
            context.put("startedAt", Instant.now().toString());
            context.put("mode", "PREVIEW");
            for (String field : List.of("processInstanceId", "businessKey", "documentNo", "applicantAccount",
                    "initiatorAccount", "applicantOrgUnitId")) {
                context.putNull(field);
            }
            ObjectNode request = JSON.createObjectNode();
            request.put("protocolVersion", 2);
            request.put("profile", FmGroovyDraftValidator.PROFILE);
            request.put("invocationId", invocationId);
            request.put("attemptId", UUID.randomUUID().toString());
            request.put("generation", 1);
            request.put("bindingSha256", hash);
            request.put("operation", operation);
            request.put("script", binding.getScriptContent());
            request.set("input", input);
            request.set("context", context);
            request.set("inputSchema", inputSchema);
            request.set("outputSchema", FmGroovyContractJson.object(binding.getOutputSchema()));
            byte[] bytes = JSON.writeValueAsBytes(request);
            if (bytes.length > 768 * 1024) {
                throw new ServiceException("GROOVY_PREVIEW_REQUEST_INVALID");
            }
            current(command, version);
            long start = System.nanoTime();
            byte[] response = runner.execute(bytes, binding.getTimeoutMs());
            current(command, version);
            var view = FmGroovyPreviewProtocol.response(response, request, manifest.sha256(),
                    command.expectedLockVersion(), (System.nanoTime() - start) / 1000000);
            DefaultResult<FmGroovyPreviewView> result = new DefaultResult<>();
            result.setSuccess(YesNoKeyProvide.YES);
            result.setValue(view);
            return result;
        } catch (IllegalArgumentException invalid) {
            throw new ServiceException("GROOVY_PREVIEW_CONTRACT_INVALID");
        }
    }

    private FmProcessVersion current(FmGroovyPreviewCommand command, FmProcessVersion original)
            throws ServiceException {
        FmProcessVersion version = versions.selectByPrimaryKey(command.oid()).getValueEmptyThrowMessage();
        access.require(version.getTenantId());
        if (!"DRAFT".equals(version.getVersionStatus())
                || !command.expectedLockVersion().equals(versions.findDraftLockVersion(version.getTenantId(), command.oid()))
                || original != null && (!original.getTenantId().equals(version.getTenantId())
                        || !original.getProcessDefId().equals(version.getProcessDefId())
                        || !original.getVersionNo().equals(version.getVersionNo()))) {
            throw new ServiceException("GROOVY_PREVIEW_STALE_VERSION");
        }
        return version;
    }
}
