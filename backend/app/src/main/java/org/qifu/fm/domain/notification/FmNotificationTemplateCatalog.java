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
			case "TASK_DUE_SOON" -> "FMTASKDUE";
			case "TASK_OVERDUE" -> "FMTASKOVD";
			default -> "FMPROCHG";
		};
	}

	private TemplateText fallback(String eventType, String referenceId, String taskName) {
		if ("TASK_ADMIN_REASSIGNED_FROM".equals(eventType)) {
			return new TemplateText("簽核工作已被管理員改派",
					StringUtils.defaultIfBlank(taskName, "流程簽核工作"));
		}
		if ("TASK_ADMIN_REASSIGNED_TO".equals(eventType)) {
			return new TemplateText("收到管理員改派的簽核工作",
					StringUtils.defaultIfBlank(taskName, "流程簽核工作"));
		}
		if ("TASK_ASSIGNED".equals(eventType)) {
			return new TemplateText("你有新的流程待辦",
					StringUtils.defaultIfBlank(taskName, "流程待辦"));
		}
		if ("TASK_DUE_SOON".equals(eventType)) {
			return new TemplateText("流程待辦即將到期",
					StringUtils.defaultIfBlank(taskName, "流程待辦"));
		}
		if ("TASK_OVERDUE".equals(eventType)) {
			return new TemplateText("流程待辦已逾時",
					StringUtils.defaultIfBlank(taskName, "流程待辦"));
		}
		String title = switch (eventType) {
			case "PARALLEL_ADD_SIGN_ASSIGNED" -> "你有新的平行加簽待辦";
			case "PARALLEL_ADD_SIGN_REPLIED" -> "平行加簽已有回覆";
			case "PARALLEL_ADD_SIGN_COMPLETED" -> "平行加簽已全部完成";
			case "PARALLEL_ADD_SIGN_CANCELLED" -> "平行加簽已取消";
			case "PROCESS_COMPLETED" -> "你的流程已完成";
			case "PROCESS_REJECTED" -> "你的流程已駁回";
			case "PROCESS_CANCELLED" -> "流程已取消";
			case "PROCESS_TERMINATED" -> "流程已由管理員終止";
			default -> "流程狀態已更新";
		};
		String referenceLabel = eventType.startsWith("PARALLEL_ADD_SIGN_")
				? "參考編號：" : "流程編號：";
		return new TemplateText(title, referenceLabel + referenceId);
	}

	interface TemplateRenderer {
		TemplateResultObj render(String templateId, Object data) throws Exception;
	}

	public record TemplateContext(String referenceId, String taskName) {
	}

	public record TemplateText(String subject, String content) {
	}
}
