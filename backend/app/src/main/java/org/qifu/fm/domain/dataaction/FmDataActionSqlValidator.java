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

	private static final Pattern PARAMETER_PATTERN = Pattern.compile(
			"(?<!:):([A-Za-z][A-Za-z0-9_]*)");
	private static final Set<String> STATEMENT_TYPES = Set.of(
			"SELECT_ONE", "SELECT_LIST", "INSERT", "UPDATE", "DELETE");

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
		if (!"ONCE".equals(normalized(step.getExecutionMode()))) {
			throw new ServiceException("第一版目前僅支援 ONCE 執行模式");
		}
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
