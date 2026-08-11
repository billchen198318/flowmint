package org.qifu.fm.domain.notification;

import org.apache.commons.lang3.StringUtils;
import org.qifu.core.model.TemplateResultObj;
import org.qifu.core.util.TemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FmNotificationTemplateCatalog {

	private static final Logger LOGGER = LoggerFactory.getLogger(
			FmNotificationTemplateCatalog.class);

	private final TemplateRenderer renderer;

	public FmNotificationTemplateCatalog() {
		this(TemplateUtils::getResult);
	}

	FmNotificationTemplateCatalog(TemplateRenderer renderer) {
		this.renderer = renderer;
	}

	public TemplateText render(String eventType, String referenceId, String taskName) {
		TemplateText fallback = fallback(eventType, referenceId, taskName);
		try {
			TemplateResultObj result = renderer.render(
					templateId(eventType),
					new TemplateContext(referenceId,
							StringUtils.defaultIfBlank(taskName, "流程待辦")));
			if (result == null || StringUtils.isAnyBlank(
					result.getTitle(), result.getContent())) {
				return fallback;
			}
			return new TemplateText(result.getTitle(), result.getContent());
		} catch (Exception exception) {
			LOGGER.warn("FlowMint 通知範本 {} 載入失敗，使用內建文字：{}",
					templateId(eventType), exception.getMessage());
			return fallback;
		}
	}

	private String templateId(String eventType) {
		return switch (eventType) {
			case "TASK_ASSIGNED" -> "FMTASKASG";
			case "PROCESS_COMPLETED" -> "FMPROCMP";
			case "PROCESS_REJECTED" -> "FMPROREJ";
			case "PROCESS_CANCELLED" -> "FMPROCAN";
			default -> "FMPROCHG";
		};
	}

	private TemplateText fallback(String eventType, String referenceId, String taskName) {
		if ("TASK_ASSIGNED".equals(eventType)) {
			return new TemplateText("你有新的流程待辦",
					StringUtils.defaultIfBlank(taskName, "流程待辦"));
		}
		String title = switch (eventType) {
			case "PROCESS_COMPLETED" -> "你的流程已完成";
			case "PROCESS_REJECTED" -> "你的流程已駁回";
			case "PROCESS_CANCELLED" -> "流程已取消";
			case "PROCESS_TERMINATED" -> "流程已由管理員終止";
			default -> "流程狀態已更新";
		};
		return new TemplateText(title, "流程編號：" + referenceId);
	}

	interface TemplateRenderer {
		TemplateResultObj render(String templateId, Object data) throws Exception;
	}

	public record TemplateContext(String referenceId, String taskName) {
	}

	public record TemplateText(String subject, String content) {
	}
}
