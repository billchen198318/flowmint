package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.qifu.fm.dto.view.FmTaskActionView;
import org.qifu.fm.dto.view.FmTaskDetailView;
import org.qifu.fm.dto.view.FmTaskInboxView;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

class FmAiContextBuilderPrivacyTest {
	private final ObjectMapper mapper = new ObjectMapper();
	private final FmAiContextBuilder builder = new FmAiContextBuilder(mapper);

	@Test
	void gridOnlyIncludesVisibleDeclaredFields() throws Exception {
		var schema = mapper.readTree("""
				[{"type":"datagrid","key":"items","label":"Items","components":[
				  {"type":"textfield","key":"name","label":"Name"},
				  {"type":"textfield","key":"privateNote","hidden":true},
				  {"type":"password","key":"pin"}
				]}]
				""");
		ArrayNode output = mapper.createArrayNode();
		builder.collectComponents(schema, Map.of("items", List.of(Map.of(
				"name", "visible", "privateNote", "do not send",
				"pin", "1234", "undeclared", "also private"))), output, "");

		assertEquals("visible", output.get(0).path("value").get(0).path("name").asText());
		assertFalse(output.toString().contains("do not send"));
		assertFalse(output.toString().contains("1234"));
		assertFalse(output.toString().contains("also private"));
	}

	@Test
	void containerReadsNestedDataWithoutExposingUnknownFields() throws Exception {
		var schema = mapper.readTree("""
				[{"type":"container","key":"contact","components":[
				  {"type":"textfield","key":"name","label":"Name"}
				]}]
				""");
		ArrayNode output = mapper.createArrayNode();
		builder.collectComponents(schema, Map.of("contact", Map.of(
				"name", "Alice", "privateNote", "do not send")), output, "");

		assertEquals("contact.name", output.get(0).path("key").asText());
		assertEquals("Alice", output.get(0).path("value").asText());
		assertFalse(output.toString().contains("do not send"));
	}

	@Test
	void historyKeepsMostRecentFiftyActions() throws Exception {
		List<FmTaskActionView> actions = IntStream.range(0, 51)
				.mapToObj(index -> new FmTaskActionView("APPROVE", "DONE", "actor",
						"comment-" + index, null, null)).toList();
		FmTaskDetailView detail = new FmTaskDetailView(
				new FmTaskInboxView("task", "key", "Task", "process", "business",
						"number", "Process", "applicant", null, null, false),
				null, null, null, "{\"components\":[]}", null, null, null,
				Map.of(), 1, false, false, false, false, false, false, 0,
				false, false, false, null, null, null, null, actions);
		var history = builder.build(detail).path("history");
		assertEquals(50, history.size());
		assertEquals("comment-1", history.get(0).path("comment").asText());
		assertEquals("comment-50", history.get(49).path("comment").asText());
	}
}
