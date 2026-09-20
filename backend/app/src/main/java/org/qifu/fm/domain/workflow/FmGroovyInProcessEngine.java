package org.qifu.fm.domain.workflow;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Bounded protocol adapter around the Groovy compiler already packaged in the application. */
final class FmGroovyInProcessEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(FmGroovyInProcessEngine.class);
    static final String PROFILE = "groovy-5.0.6-java21-trusted1";
    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build();

    byte[] execute(byte[] bytes) {
        Request request;
        try {
            request = request(bytes);
        } catch (Exception invalid) {
            return json(failure(null, "REQUEST_INVALID"));
        }
        try {
            if ("RUN".equals(request.operation)) {
                FmGroovyContractJson.validateValue(request.input, request.inputSchema);
            }
        } catch (Exception invalid) {
            return json(failure(request, "GROOVY_INPUT_INVALID"));
        }
        try (GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader(), configuration())) {
            Class<?> compiled = loader.parseClass(request.script, "FlowMintScript.groovy");
            if ("CHECK".equals(request.operation)) {
                return json(response(request, "VALID"));
            }
            Binding binding = new Binding();
            binding.setVariable("input", mutableMap(request.input));
            binding.setVariable("context", Collections.unmodifiableMap(mutableMap(request.context)));
            BoundedLog log = new BoundedLog();
            binding.setVariable("log", log);
            Script script = InvokerHelper.createScript(compiled, binding);
            Object output;
            try {
                output = script.run();
            } catch (Exception failed) {
                return json(failure(request, "GROOVY_RUNTIME_ERROR"));
            }
            try {
                JsonNode result = JSON.valueToTree(output);
                if (!result.isObject() || JSON.writeValueAsBytes(result).length > 256 * 1024) {
                    throw new IllegalArgumentException();
                }
                FmGroovyContractJson.validateValue(result, request.outputSchema);
                var response = response(request, "SUCCEEDED");
                response.put("result", JSON.treeToValue(result, Object.class));
                response.put("logs", log.messages);
                response.put("logsTruncated", log.truncated);
                return json(response);
            } catch (Exception invalid) {
                return json(failure(request, "GROOVY_OUTPUT_INVALID"));
            }
        } catch (CompilationFailedException failed) {
            return json(compilationFailure(request, failed));
        } catch (SecurityException denied) {
            return json(failure(request, "GROOVY_POLICY_DENIED"));
        } catch (Exception failed) {
            LOGGER.error("Groovy in-process engine failed before producing a protocol response", failed);
            return json(failure(request, "WORKER_ERROR"));
        }
    }

    private static Request request(byte[] bytes) {
        if (bytes == null || bytes.length > 768 * 1024) throw new IllegalArgumentException();
        JsonNode node = JSON.readTree(bytes);
        if (node == null || !node.isObject() || node.path("protocolVersion").asInt(-1) != 2
                || !PROFILE.equals(node.path("profile").asText()) || node.path("generation").asLong(0) < 1
                || !node.path("invocationId").asText().matches("[A-Za-z0-9_-]{1,64}")
                || !node.path("attemptId").asText().matches("[A-Za-z0-9_-]{1,64}")
                || !node.path("bindingSha256").asText().matches("[a-f0-9]{64}")) throw new IllegalArgumentException();
        String operation = node.path("operation").asText();
        String script = FmGroovyContractJson.normalizeScript(node.path("script").asText(null));
        if (!("CHECK".equals(operation) || "RUN".equals(operation))) throw new IllegalArgumentException();
        JsonNode input = node.get("input"), context = node.get("context");
        JsonNode inputSchema = node.get("inputSchema"), outputSchema = node.get("outputSchema");
        if (input == null || !input.isObject() || context == null || !context.isObject()
                || inputSchema == null || outputSchema == null) throw new IllegalArgumentException();
        FmGroovyContractJson.validateSchema(inputSchema);
        FmGroovyContractJson.validateSchema(outputSchema);
        if (!"object".equals(inputSchema.path("type").asText())
                || !"object".equals(outputSchema.path("type").asText())) throw new IllegalArgumentException();
        return new Request(node.path("profile").asText(), node.path("invocationId").asText(),
                node.path("attemptId").asText(), node.path("generation").asLong(),
                node.path("bindingSha256").asText(), operation, script, input, context, inputSchema, outputSchema);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mutableMap(JsonNode node) {
        return new LinkedHashMap<>((Map<String, Object>) JSON.treeToValue(node, Object.class));
    }

    private static byte[] json(Object value) {
        return JSON.writeValueAsBytes(value);
    }

    private static Map<String, Object> response(Request request, String status) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("protocolVersion", 2); value.put("profile", request.profile);
        value.put("invocationId", request.invocationId); value.put("attemptId", request.attemptId);
        value.put("generation", request.generation); value.put("bindingSha256", request.bindingSha256);
        value.put("status", status); return value;
    }

    private static Map<String, Object> failure(Request request, String code) {
        Map<String, Object> value = request == null ? new LinkedHashMap<>() : response(request, "FAILED");
        value.put("protocolVersion", 2); value.put("status", "FAILED"); value.put("errorCode", code); return value;
    }

    private static Map<String, Object> compilationFailure(Request request, CompilationFailedException error) {
        String code = "GROOVY_COMPILE_ERROR"; List<Map<String, Object>> diagnostics = new ArrayList<>();
        if (error instanceof MultipleCompilationErrorsException multiple) {
            for (var message : multiple.getErrorCollector().getErrors()) {
                if (message instanceof SyntaxErrorMessage syntax && diagnostics.size() < 20) {
                    int line = syntax.getCause().getStartLine(), column = syntax.getCause().getStartColumn();
                    if (line > 0 && column > 0) diagnostics.add(diagnostic(code, line, column));
                }
            }
        }
        Map<String, Object> value = failure(request, code);
        if (!diagnostics.isEmpty()) value.put("diagnostics", diagnostics);
        return value;
    }

    private static Map<String, Object> diagnostic(String code, int line, int column) {
        return Map.of("code", code, "line", line, "column", column);
    }

    private static CompilerConfiguration configuration() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setDisabledGlobalASTTransformations(Set.of("groovy.grape.GrabAnnotationTransformation",
                "org.codehaus.groovy.ast.builder.AstBuilderTransformation"));
        return configuration;
    }

    private record Request(String profile, String invocationId, String attemptId, long generation,
            String bindingSha256, String operation, String script, JsonNode input, JsonNode context,
            JsonNode inputSchema, JsonNode outputSchema) { }

    public static final class BoundedLog {
        private final List<String> messages = new ArrayList<>(); private int bytes; private boolean truncated;
        public void info(String message) {
            if (message == null) return;
            int length = message.getBytes(StandardCharsets.UTF_8).length;
            if (messages.size() >= 100 || length > 16 * 1024 - bytes) { truncated = true; return; }
            messages.add(message); bytes += length;
        }
    }
}
