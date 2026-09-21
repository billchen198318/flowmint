package org.qifu.fm.domain.workflow;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessSystemTask;
import org.qifu.fm.entity.FmProcessVersion;

import tools.jackson.databind.JsonNode;

/** Draft structure validation only; never compiles or runs a script. */
public final class FmGroovyDraftValidator {

    public static final String PROFILE = "groovy-5.0.6-java21-trusted1";
    public static final Set<String> CONTEXT_NAMES = Set.of(
            "tenantId", "processInstanceId", "processVersionNo", "nodeId", "invocationId",
            "startedAt", "mode", "businessKey", "documentNo", "applicantAccount",
            "initiatorAccount", "applicantOrgUnitId");

    private static final String NAMESPACE = "https://flowmint.qifu.org/schema/bpmn";
    private static final Set<String> FORBIDDEN_PATH_PARTS = Set.of(
            "class", "metaClass", "__proto__", "prototype", "constructor");
    private static final Set<String> SYSTEM_FIELDS = Set.of(
            "tenantId", "applicantAccount", "initiatorAccount", "applicantOrgUnitId",
            "documentNo", "documentNumber", "businessKey", "processInstanceId", "processStatus",
            "versionNo", "formDataId", "formVersionNo", "revisionNo", "lockVersion", "attachmentIds");

    public List<FmProcessSystemTask> validate(FmProcessVersion version, String xml,
            List<FmGroovyBindingCommand> commands) throws ServiceException {
        try {
            Map<String, String> nodes = nodes(xml);
            List<FmGroovyBindingCommand> bindings = commands == null ? List.of() : commands;
            if (bindings.size() > 100 || nodes.size() != bindings.size()) {
                throw new IllegalArgumentException("Groovy 節點與 binding 數量不一致或超過上限");
            }
            Set<String> seenNodes = new HashSet<>();
            Set<String> seenBindings = new HashSet<>();
            List<FmProcessSystemTask> result = new ArrayList<>();
            for (FmGroovyBindingCommand command : bindings) {
                if (command == null || !identifier(command.nodeId()) || !identifier(command.bindingId())
                        || !command.bindingId().equals(nodes.get(command.nodeId()))
                        || !seenNodes.add(command.nodeId()) || !seenBindings.add(command.bindingId())) {
                    throw new IllegalArgumentException("Groovy nodeId／bindingId 無效或重複");
                }
                String script = FmGroovyContractJson.normalizeScript(command.scriptContent());
                if (command.timeoutMs() == null || command.timeoutMs() < 100 || command.timeoutMs() > 10000) {
                    throw new IllegalArgumentException("Groovy timeout 必須介於 100～10000 ms");
                }
                JsonNode input = FmGroovyContractJson.object(command.inputSchema());
                JsonNode output = FmGroovyContractJson.object(command.outputSchema());
                FmGroovyContractJson.validateSchema(input);
                FmGroovyContractJson.validateSchema(output);
                if (!"object".equals(input.path("type").asText())
                        || !"object".equals(output.path("type").asText())) {
                    throw new IllegalArgumentException("Groovy 輸入／輸出 Schema 根型別必須為 object");
                }
                JsonNode mapping = FmGroovyContractJson.object(command.mappingContent());
                validateMapping(mapping, input, output);
                FmProcessSystemTask binding = new FmProcessSystemTask();
                binding.setTenantId(version.getTenantId());
                binding.setProcessDefId(version.getProcessDefId());
                binding.setVersionNo(version.getVersionNo());
                binding.setNodeId(command.nodeId());
                binding.setBindingId(command.bindingId());
                binding.setTaskType("GROOVY");
                binding.setScriptContent(script);
                binding.setInputSchema(FmGroovyContractJson.canonical(input));
                binding.setOutputSchema(FmGroovyContractJson.canonical(output));
                binding.setMappingContent(FmGroovyContractJson.canonical(mapping));
                binding.setTimeoutMs(command.timeoutMs());
                binding.setEngineProfile(PROFILE);
                binding.setPolicyVersion("1");
                binding.setLockVersion(0);
                binding.setContentSha256(FmGroovyContractJson.bindingHash(script, input, output, mapping,
                        command.timeoutMs(), PROFILE, "1"));
                result.add(binding);
            }
            return result;
        } catch (Exception exception) {
            throw new ServiceException("Groovy 草稿驗證失敗：" + exception.getMessage());
        }
    }

    public static FmGroovyBindingCommand command(FmProcessSystemTask binding) {
        return new FmGroovyBindingCommand(binding.getNodeId(), binding.getBindingId(),
                binding.getScriptContent(), binding.getInputSchema(), binding.getOutputSchema(),
                binding.getMappingContent(), binding.getTimeoutMs());
    }

    private Map<String, String> nodes(String xml) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));
        Map<String, String> result = new HashMap<>();
        try {
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT
                        && "http://www.omg.org/spec/BPMN/20100524/MODEL".equals(reader.getNamespaceURI())
                        && "serviceTask".equals(reader.getLocalName())
                        && "GROOVY".equals(reader.getAttributeValue(NAMESPACE, "taskType"))) {
                    String id = reader.getAttributeValue(null, "id");
                    if (result.put(id, reader.getAttributeValue(NAMESPACE, "bindingId")) != null) {
                        throw new IllegalArgumentException("重複 Groovy nodeId");
                    }
                }
            }
        } finally {
            reader.close();
        }
        return result;
    }

    public static void validateMapping(JsonNode mapping, JsonNode input, JsonNode output) {
        if (!mapping.path("mappingVersion").isInt() || mapping.path("mappingVersion").asInt() != 1
                || !mapping.path("input").isObject() || !mapping.path("output").isObject()
                || !Set.of("mappingVersion", "input", "output").containsAll(mapping.propertyNames())) {
            throw new IllegalArgumentException("必須提供 Groovy Mapping V1");
        }
        JsonNode inputs = mapping.path("input");
        for (String name : inputs.propertyNames()) {
            JsonNode entry = inputs.get(name);
            if (!identifier(name) || name.contains("-") || !input.path("properties").has(name) || !entry.isObject()) {
                throw new IllegalArgumentException("輸入名稱必須是 Schema 已宣告的腳本變數");
            }
            String source = entry.path("source").asText();
            String path = entry.path("path").asText();
            Set<String> keys = "CONSTANT".equals(source) ? Set.of("source", "value") : Set.of("source", "path");
            if (!keys.containsAll(entry.propertyNames())
                    || !("CONSTANT".equals(source) && entry.has("value")
                    || "FORM_DATA".equals(source) && validPath(path)
                    || "PROCESS_CONTEXT".equals(source) && CONTEXT_NAMES.contains(path))) {
                throw new IllegalArgumentException("不支援的 Groovy 輸入來源");
            }
            if ("CONSTANT".equals(source)) {
                FmGroovyContractJson.validateValue(entry.get("value"), input.path("properties").get(name));
            }
        }
        for (JsonNode required : input.path("required")) {
            if (!required.isString() || !inputs.has(required.asText())) {
                throw new IllegalArgumentException("必要輸入缺少 Mapping");
            }
        }
        Set<String> targets = new HashSet<>();
        for (String name : mapping.path("output").propertyNames()) {
            JsonNode entry = mapping.path("output").get(name);
            if (!output.path("properties").has(name) || !entry.isObject()
                    || !Set.of("target", "path").containsAll(entry.propertyNames())) {
                throw new IllegalArgumentException("輸出名稱必須存在於 Schema");
            }
            String target = entry.path("target").asText();
            String path = entry.path("path").asText();
            if ("DISCARD".equals(target) && !entry.has("path")) {
                continue;
            }
            if (!"FORM_DATA".equals(target) || !validPath(path)
                    || SYSTEM_FIELDS.contains(path.split("\\.")[0])) {
                throw new IllegalArgumentException("不允許回寫此欄位");
            }
            if (targets.stream().anyMatch(old -> old.equals(path) || old.startsWith(path + ".")
                    || path.startsWith(old + "."))) {
                throw new IllegalArgumentException("輸出 path 重複或父子衝突");
            }
            targets.add(path);
        }
    }

    private static boolean identifier(String value) {
        return value != null && value.matches("[A-Za-z][A-Za-z0-9_-]{0,99}");
    }

    private static boolean validPath(String value) {
        return value.matches("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)*")
                && java.util.Arrays.stream(value.split("\\.")).noneMatch(FORBIDDEN_PATH_PARTS::contains);
    }
}
