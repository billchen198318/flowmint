package org.qifu.fm.domain.dataaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class FmDataActionContinueConditionEvaluator {
	private static final Pattern ATOM = Pattern.compile(
			"^\\s*\\$\\{([^}]+)}(?:\\s*(==|!=|>=|<=|>|<)\\s*(.+))?\\s*$");
	private final FmDataActionParameterResolver resolver;

	public FmDataActionContinueConditionEvaluator(FmDataActionParameterResolver resolver) {
		this.resolver = resolver;
	}

	public boolean evaluate(String expression, Map<String, Object> request,
			Map<String, Object> steps) throws ServiceException {
		if (expression == null || expression.isBlank()) return true;
		Map<String, Object> context = new LinkedHashMap<>();
		context.put("request", request);
		context.put("steps", steps);
		for (String orPart : split(expression, "||")) {
			boolean all = true;
			for (String andPart : split(orPart, "&&")) all &= atom(andPart, context);
			if (all) return true;
		}
		return false;
	}

	public void validate(String expression) throws ServiceException {
		evaluate(expression, Map.of(), Map.of());
	}

	private boolean atom(String expression, Map<String, Object> context)
			throws ServiceException {
		Matcher matcher = ATOM.matcher(expression);
		if (!matcher.matches()) throw new ServiceException("Continue Condition 格式不正確");
		Object actual = resolver.readRequestPath(context, "$." + matcher.group(1));
		if (matcher.group(2) == null) return actual instanceof Boolean b ? b : actual != null;
		Object expected = literal(matcher.group(3).trim());
		int comparison = compare(actual, expected);
		return switch (matcher.group(2)) {
		case "==" -> comparison == 0; case "!=" -> comparison != 0;
		case ">" -> comparison > 0; case ">=" -> comparison >= 0;
		case "<" -> comparison < 0; case "<=" -> comparison <= 0;
		default -> false;
		};
	}

	private Object literal(String value) {
		if ((value.startsWith("\"") && value.endsWith("\""))
				|| (value.startsWith("'") && value.endsWith("'")))
			return value.substring(1, value.length() - 1);
		if ("null".equalsIgnoreCase(value)) return null;
		if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))
			return Boolean.valueOf(value);
		try { return new BigDecimal(value); } catch (NumberFormatException ignored) { return value; }
	}

	private int compare(Object left, Object right) {
		if (left == null || right == null) return left == right ? 0 : left == null ? -1 : 1;
		try { return new BigDecimal(String.valueOf(left)).compareTo(new BigDecimal(String.valueOf(right))); }
		catch (NumberFormatException ignored) { }
		try { return Instant.parse(String.valueOf(left)).compareTo(Instant.parse(String.valueOf(right))); }
		catch (Exception ignored) { }
		try { return LocalDate.parse(String.valueOf(left)).compareTo(LocalDate.parse(String.valueOf(right))); }
		catch (Exception ignored) { }
		return String.valueOf(left).compareTo(String.valueOf(right));
	}

	private List<String> split(String expression, String operator) throws ServiceException {
		List<String> parts = new ArrayList<>(); boolean quoted = false; char quote = 0; int start = 0;
		for (int i = 0; i < expression.length() - 1; i++) {
			char c = expression.charAt(i);
			if ((c == '\'' || c == '\"') && (!quoted || c == quote)) { quoted = !quoted; quote = c; }
			if (!quoted && expression.startsWith(operator, i)) { parts.add(expression.substring(start, i)); start = i + 2; i++; }
		}
		if (quoted) throw new ServiceException("Continue Condition 引號未結束");
		parts.add(expression.substring(start)); return parts;
	}
}
