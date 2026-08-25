package org.qifu.fm.domain.dataaction;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.fm.entity.FmDataActionStep;
import org.springframework.stereotype.Component;

@Component
public class FmDataActionSqlValidator {
	private final FmDataActionContinueConditionEvaluator conditionEvaluator;

	public FmDataActionSqlValidator(FmDataActionContinueConditionEvaluator conditionEvaluator) {
		this.conditionEvaluator = conditionEvaluator;
	}

	private static final Pattern PARAMETER_PATTERN = Pattern.compile(
			"(?<!:):([A-Za-z][A-Za-z0-9_]*)");
	private static final Set<String> STATEMENT_TYPES = Set.of(
			"SELECT_ONE", "SELECT_LIST", "INSERT", "UPDATE", "DELETE");
	private static final Set<String> EXECUTION_MODES = Set.of("ONCE", "FOR_EACH");

	public void validate(FmDataActionStep step, String actionType,
			Set<String> availableParameters) throws ServiceException {
		String statementType = normalized(step.getStatementType());
		String sql = step.getSqlContent() == null ? "" : step.getSqlContent().trim();
		if (!STATEMENT_TYPES.contains(statementType)
				|| sql.isBlank()
				|| sql.contains(";")
				|| sql.contains("${")
				|| sql.contains("--")
				|| sql.contains("/*")) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		String expectedKeyword = statementType.startsWith("SELECT")
				? "SELECT" : statementType;
		if (!sql.toUpperCase(Locale.ROOT).startsWith(expectedKeyword + " ")) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		if ("QUERY".equals(normalized(actionType))
				&& !statementType.startsWith("SELECT")) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		String executionMode = normalized(step.getExecutionMode());
		if (!EXECUTION_MODES.contains(executionMode)) {
			throw new ServiceException("不支援的執行模式");
		}
		if ("FOR_EACH".equals(executionMode)
				&& (step.getArrayPath() == null
						|| !step.getArrayPath().trim().startsWith("$.")
						|| statementType.startsWith("SELECT"))) {
			throw new ServiceException("FOR_EACH 僅支援異動 SQL，且必須設定 Array Path");
		}
		if ("GENERATED_KEY".equals(normalized(step.getResultMode()))
				&& !"INSERT".equals(statementType)) {
			throw new ServiceException("GENERATED_KEY 僅適用於 INSERT");
		}
		conditionEvaluator.validate(step.getContinueCondition());
		Matcher matcher = PARAMETER_PATTERN.matcher(sql);
		while (matcher.find()) {
			if (!availableParameters.contains(matcher.group(1))) {
				throw new ServiceException("SQL 參數未定義來源：" + matcher.group(1));
			}
		}
	}

	private String normalized(String value) {
		return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
	}
}
