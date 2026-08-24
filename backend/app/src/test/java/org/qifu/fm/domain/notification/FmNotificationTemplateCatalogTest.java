package org.qifu.fm.domain.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.qifu.core.model.TemplateResultObj;

class FmNotificationTemplateCatalogTest {

	@Test
	void usesQifuTemplateWhenConfigured() {
		FmNotificationTemplateCatalog catalog = new FmNotificationTemplateCatalog(
				(templateId, data) -> result("自訂主旨", "自訂內容"));

		var rendered = catalog.render("TASK_ASSIGNED", "TASK-1", "財務簽核");

		assertEquals("自訂主旨", rendered.subject());
		assertEquals("自訂內容", rendered.content());
	}

	@Test
	void fallsBackWithoutBreakingWorkflowWhenTemplateFails() {
		FmNotificationTemplateCatalog catalog = new FmNotificationTemplateCatalog(
				(templateId, data) -> { throw new IllegalStateException("missing"); });

		var rendered = catalog.render("PROCESS_COMPLETED", "PROCESS-1", null);

		assertEquals("你的流程已完成", rendered.subject());
		assertEquals("流程編號：PROCESS-1", rendered.content());
	}

	@Test
	void providesParallelAddSignFallbackText() {
		FmNotificationTemplateCatalog catalog = new FmNotificationTemplateCatalog(
				(templateId, data) -> { throw new IllegalStateException("missing"); });

		var rendered = catalog.render(
				"PARALLEL_ADD_SIGN_CANCELLED", "TASK-1", null);

		assertEquals("平行加簽已取消", rendered.subject());
		assertEquals("參考編號：TASK-1", rendered.content());
	}

	private TemplateResultObj result(String title, String content) {
		TemplateResultObj result = new TemplateResultObj();
		result.setTitle(title);
		result.setContent(content);
		return result;
	}
}
