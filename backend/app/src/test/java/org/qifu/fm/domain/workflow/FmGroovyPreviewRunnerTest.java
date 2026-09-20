package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

import tools.jackson.databind.json.JsonMapper;

class FmGroovyPreviewRunnerTest {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void disabledConfigurationDeniesExecution() {
        var runner = new FmGroovyPreviewRunner(false, false, "", "", "", "", "");
        assertEquals("GROOVY_PREVIEW_UNAVAILABLE",
                assertThrows(ServiceException.class, () -> runner.execute(new byte[0], 3000)).getMessage());
    }

    @Test
    void executesAndChecksInsideApplicationJvm() throws Exception {
        var runner = runner();
        assertEquals("SUCCEEDED", JSON.readTree(runner.execute(
                FmGroovyInProcessTestRequest.bytes("RUN",
                        "assert context.containsKey('businessKey'); return [total: input.amount * 2G]"), 5000))
                .path("status").asText());
        assertEquals("VALID", JSON.readTree(runner.execute(
                FmGroovyInProcessTestRequest.bytes("CHECK", "return [total: input.amount * 2G]"), 5000))
                .path("status").asText());
    }

    @Test
    void supportsDocumentedJavaHttpAndGroovyJsonApis() throws Exception {
        String httpScript = """
                import java.net.URI
                import java.net.http.HttpClient
                import java.net.http.HttpRequest
                import java.net.http.HttpResponse
                import java.time.Duration
                def client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
                def request = HttpRequest.newBuilder(URI.create('https://example.com'))
                    .timeout(Duration.ofSeconds(15)).GET().build()
                return [total: 0]
                """;
        var checked = JSON.readTree(runner().execute(
                FmGroovyInProcessTestRequest.bytes("CHECK", httpScript), 5000));
        assertEquals("VALID", checked.path("status").asText());

        String jsonScript = """
                import groovy.json.JsonOutput
                import groovy.json.JsonSlurper
                String text = JsonOutput.toJson([amount: input.amount])
                Map parsed = (Map) new JsonSlurper().parseText(text)
                return [total: parsed.amount * 2G]
                """;
        var response = JSON.readTree(runner().execute(
                FmGroovyInProcessTestRequest.bytes("RUN", jsonScript), 5000));
        assertEquals("SUCCEEDED", response.path("status").asText(), response.toString());
        assertEquals(25, response.path("result").path("total").asInt());
    }

    private FmGroovyPreviewRunner runner() {
        return new FmGroovyPreviewRunner(true, false, "ignored", "ignored", "", "", "");
    }
}
