package org.qifu.fm.domain.workflow;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.qifu.fm.dto.view.FmGroovyPreviewView;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Rechecks the supervisor response at the app boundary before returning anything to the designer. */
public final class FmGroovyPreviewProtocol {

    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();
    private static final Set<String> FIELDS = Set.of("protocolVersion", "profile", "invocationId", "attemptId",
            "generation", "bindingSha256", "status", "errorCode", "result", "logs", "logsTruncated", "diagnostics");
    private static final Set<String> ERRORS = Set.of("REQUEST_INVALID", "REQUEST_TOO_LARGE", "GROOVY_INPUT_INVALID",
            "GROOVY_OUTPUT_INVALID", "GROOVY_COMPILE_ERROR", "GROOVY_POLICY_DENIED", "GROOVY_RUNTIME_ERROR", "WORKER_ERROR");

    private FmGroovyPreviewProtocol() {
    }

    public static FmGroovyPreviewView response(byte[] bytes, JsonNode request, String manifestHash,
            Integer lockVersion, long elapsedMs) {
        try {
            if (bytes == null || bytes.length > 300 * 1024) {
                throw new IllegalArgumentException();
            }
            JsonNode response = JSON.readTree(bytes);
            if (response == null || !response.isObject() || !FIELDS.containsAll(response.propertyNames())) {
                throw new IllegalArgumentException();
            }
            for (String field : List.of("protocolVersion", "profile", "invocationId", "attemptId",
                    "generation", "bindingSha256")) {
                if (!request.get(field).equals(response.get(field))) {
                    throw new IllegalArgumentException();
                }
            }
            String status = response.path("status").asText();
            String error = null;
            JsonNode result = null;
            List<String> logs = new ArrayList<>();
            List<FmGroovyPreviewView.Diagnostic> diagnostics = new ArrayList<>();
            boolean truncated = false;
            if ("FAILED".equals(status)) {
                error = response.path("errorCode").asText();
                if (!ERRORS.contains(error) || response.has("result") || response.has("logs")
                        || response.has("logsTruncated")) {
                    throw new IllegalArgumentException();
                }
                if (response.has("diagnostics")) {
                    JsonNode entries = response.get("diagnostics");
                    if (!entries.isArray() || entries.size() > 20) {
                        throw new IllegalArgumentException();
                    }
                    for (JsonNode entry : entries) {
                        if (!entry.isObject() || entry.size() != 3 || !error.equals(entry.path("code").asText())
                                || !positiveInteger(entry.path("line")) || !positiveInteger(entry.path("column"))) {
                            throw new IllegalArgumentException();
                        }
                        diagnostics.add(new FmGroovyPreviewView.Diagnostic(error,
                                entry.get("line").asInt(), entry.get("column").asInt()));
                    }
                }
            } else if ("CHECK".equals(request.path("operation").asText()) && "VALID".equals(status)) {
                if (response.has("result") || response.has("errorCode") || response.has("diagnostics")
                        || response.has("logs") || response.has("logsTruncated")) {
                    throw new IllegalArgumentException();
                }
            } else if ("RUN".equals(request.path("operation").asText()) && "SUCCEEDED".equals(status)) {
                if (!response.path("result").isObject() || !response.path("logs").isArray()
                        || !response.path("logsTruncated").isBoolean() || response.has("errorCode")
                        || response.has("diagnostics") || response.get("logs").size() > 100) {
                    throw new IllegalArgumentException();
                }
                result = FmGroovyContractJson.object(response.get("result").toString());
                FmGroovyContractJson.validateValue(result, request.get("outputSchema"));
                int logBytes = 0;
                for (JsonNode log : response.get("logs")) {
                    if (!log.isString()) {
                        throw new IllegalArgumentException();
                    }
                    logBytes += log.asText().getBytes(StandardCharsets.UTF_8).length;
                    logs.add(log.asText());
                }
                if (logBytes > 16 * 1024) {
                    throw new IllegalArgumentException();
                }
                truncated = response.get("logsTruncated").asBoolean();
            } else {
                throw new IllegalArgumentException();
            }
            return new FmGroovyPreviewView(request.get("invocationId").asText(),
                    request.get("bindingSha256").asText(), manifestHash, status, error, result,
                    List.copyOf(logs), truncated, List.copyOf(diagnostics), elapsedMs, lockVersion);
        } catch (Exception invalid) {
            throw new IllegalArgumentException("GROOVY_PREVIEW_RESPONSE_INVALID");
        }
    }

    private static boolean positiveInteger(JsonNode value) {
        return value.isIntegralNumber() && value.canConvertToInt() && value.asInt() > 0;
    }
}
