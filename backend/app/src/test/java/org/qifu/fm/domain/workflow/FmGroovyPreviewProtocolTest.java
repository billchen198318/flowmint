package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

class FmGroovyPreviewProtocolTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void acceptsSafeDiagnosticAndPreservesOutputDecimalPrecision() {
        var response = success();
        response.set("result", FmGroovyContractJson.object("{\"amount\":9007199254740993.01}"));
        assertEquals("9007199254740993.01", parse(response).result().path("amount").asText());
        response.remove("result");
        response.remove("logs");
        response.remove("logsTruncated");
        response.put("status", "FAILED");
        response.put("errorCode", "GROOVY_COMPILE_ERROR");
        response.putArray("diagnostics").addObject().put("code", "GROOVY_COMPILE_ERROR").put("line", 2).put("column", 3);
        assertEquals(2, parse(response).diagnostics().getFirst().line());
    }

    @Test
    void rejectsWrongIdentityMalformedDiagnosticsOutputAndLogs() {
        List<Consumer<ObjectNode>> mutations = List.of(
                value -> value.put("generation", 2),
                value -> value.put("generation", 1.0),
                value -> value.put("attemptId", "other"),
                value -> value.put("bindingSha256", "b".repeat(64)),
                value -> value.put("profile", "other"),
                value -> value.put("invocationId", "other"),
                value -> value.put("secret", "unexpected"),
                value -> value.put("status", "VALID"),
                value -> value.putObject("result").put("tenantId", "other"),
                value -> value.putArray("logs").add("s".repeat(16385)),
                value -> value.putArray("logs").add(123),
                value -> value.put("logsTruncated", "false"),
                value -> value.put("errorCode", "WORKER_ERROR"));
        for (var mutation : mutations) {
            var response = success();
            mutation.accept(response);
            assertEquals("GROOVY_PREVIEW_RESPONSE_INVALID",
                    assertThrows(IllegalArgumentException.class, () -> parse(response)).getMessage());
        }
    }

    @Test
    void rejectsDuplicateKeysTrailingJsonAndOversizeBeforeExposingContent() {
        for (String content : List.of(success().toString() + " {}",
                success().toString().replace("\"status\":", "\"status\":\"secret\",\"status\":"),
                "s".repeat(300 * 1024 + 1))) {
            assertThrows(IllegalArgumentException.class, () -> FmGroovyPreviewProtocol.response(
                    content.getBytes(java.nio.charset.StandardCharsets.UTF_8), request(), "manifest", 2, 1));
        }
    }

    private org.qifu.fm.dto.view.FmGroovyPreviewView parse(ObjectNode response) {
        return FmGroovyPreviewProtocol.response(JSON.writeValueAsBytes(response), request(), "manifest", 2, 1);
    }

    private ObjectNode request() {
        var request = JSON.createObjectNode();
        request.put("protocolVersion", 2);
        request.put("profile", FmGroovyDraftValidator.PROFILE);
        request.put("invocationId", "invocation-1");
        request.put("attemptId", "attempt-1");
        request.put("generation", 1);
        request.put("bindingSha256", "a".repeat(64));
        request.put("operation", "RUN");
        request.set("outputSchema", FmGroovyContractJson.object(
                "{\"type\":\"object\",\"properties\":{\"amount\":{\"type\":\"number\"}}}"));
        return request;
    }

    private ObjectNode success() {
        var response = request();
        response.remove("operation");
        response.remove("outputSchema");
        response.put("status", "SUCCEEDED");
        response.putObject("result").put("amount", 25);
        response.putArray("logs");
        response.put("logsTruncated", false);
        return response;
    }
}
