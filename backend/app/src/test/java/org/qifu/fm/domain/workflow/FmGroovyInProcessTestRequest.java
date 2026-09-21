package org.qifu.fm.domain.workflow;

import tools.jackson.databind.json.JsonMapper;

final class FmGroovyInProcessTestRequest {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private FmGroovyInProcessTestRequest() { }

    static byte[] bytes(String operation, String script) {
        var request = JSON.createObjectNode();
        request.put("protocolVersion", 2); request.put("profile", FmGroovyInProcessEngine.PROFILE);
        request.put("invocationId", "local-test"); request.put("attemptId", "local-attempt");
        request.put("generation", 1); request.put("bindingSha256", "a".repeat(64));
        request.put("operation", operation); request.put("script", script);
        request.putObject("input").put("amount", 12.5);
        request.putObject("context").putNull("businessKey");
        var input = request.putObject("inputSchema"); input.put("type", "object");
        input.putObject("properties").putObject("amount").put("type", "number");
        var output = request.putObject("outputSchema"); output.put("type", "object");
        output.putObject("properties").putObject("total").put("type", "number");
        return JSON.writeValueAsBytes(request);
    }
}
