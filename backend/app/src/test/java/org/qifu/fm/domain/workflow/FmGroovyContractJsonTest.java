package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

class FmGroovyContractJsonTest {

    @TestFactory
    Stream<DynamicTest> sharedWorkerSchemaContract() throws Exception {
        JsonNode cases = JsonMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build().readTree(
                Files.readString(Path.of("src/test/resources/groovy-schema-v1.json")));
        return cases.valueStream().map(example -> DynamicTest.dynamicTest(example.get("name").asText(), () -> {
            // Reparse with the production parser to preserve decimal precision.
            JsonNode fixture = FmGroovyContractJson.object(example.toString());
            JsonNode schema = fixture.get("schema");
            if (!fixture.get("schemaValid").asBoolean()) {
                assertThrows(IllegalArgumentException.class, () -> FmGroovyContractJson.validateSchema(schema));
            } else {
                assertDoesNotThrow(() -> FmGroovyContractJson.validateSchema(schema));
                if (fixture.get("valueValid").asBoolean()) {
                    assertDoesNotThrow(() -> FmGroovyContractJson.validateValue(fixture.get("value"), schema));
                } else {
                    assertThrows(IllegalArgumentException.class,
                            () -> FmGroovyContractJson.validateValue(fixture.get("value"), schema));
                }
            }
        }));
    }

    @Test
    void canonicalizesObjectKeysAndDecimalRepresentationsButPreservesArrayOrder() {
        String first = FmGroovyContractJson.canonical(FmGroovyContractJson.object(
                "{\"z\":[1.00,2],\"a\":{\"b\":0.100,\"a\":true}}"));
        assertEquals(first, FmGroovyContractJson.canonical(FmGroovyContractJson.object(
                "{\"a\":{\"a\":true,\"b\":1e-1},\"z\":[1,2.0]}")));
        assertNotEquals(first, FmGroovyContractJson.canonical(FmGroovyContractJson.object(
                "{\"a\":{\"a\":true,\"b\":0.1},\"z\":[2,1]}")));
    }

    @Test
    void rejectsAmbiguousJsonWithoutReturningSource() {
        for (String content : new String[] {"{\"secret\":1,\"secret\":2}", "{} {}", "{secret}"}) {
            assertEquals("GROOVY_JSON_INVALID", assertThrows(IllegalArgumentException.class,
                    () -> FmGroovyContractJson.object(content)).getMessage());
        }
    }

    @Test
    void boundsDepthCountAndBytes() {
        assertThrows(IllegalArgumentException.class,
                () -> FmGroovyContractJson.object("{\"a\":".repeat(21) + "0" + "}".repeat(21)));
        assertThrows(IllegalArgumentException.class,
                () -> FmGroovyContractJson.object("{\"a\":[" + "0,".repeat(10000) + "0]}"));
        assertThrows(IllegalArgumentException.class,
                () -> FmGroovyContractJson.object("{\"a\":\"" + "中".repeat(90000) + "\"}"));
    }

    @Test
    void rejectsUnpairedSurrogatesBeforeHashingOrSaving() {
        String invalid = Character.toString((char) 0xD800);
        assertThrows(IllegalArgumentException.class,
                () -> FmGroovyContractJson.normalizeScript("return '" + invalid + "'"));
        assertThrows(IllegalArgumentException.class,
                () -> FmGroovyContractJson.object("{\"text\":\"\\uD800\"}"));
        assertThrows(IllegalArgumentException.class,
                () -> FmGroovyContractJson.object("{\"\\uDC00\":1}"));
        assertEquals("return '😀'\n", FmGroovyContractJson.normalizeScript("\uFEFFreturn '😀'\r\n"));
    }
}
