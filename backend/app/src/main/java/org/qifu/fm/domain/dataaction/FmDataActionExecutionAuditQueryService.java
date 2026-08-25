package org.qifu.fm.domain.dataaction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.PageOf;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.view.FmDataActionExecutionAuditView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FmDataActionExecutionAuditQueryService {

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final FmTenantAccessGuard tenantAccessGuard;

	public FmDataActionExecutionAuditQueryService(
			@Qualifier("db1JdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate,
			FmTenantAccessGuard tenantAccessGuard) {
		this.jdbcTemplate = jdbcTemplate;
		this.tenantAccessGuard = tenantAccessGuard;
	}

	public AuditPage find(Map<String, String> filters, PageOf pageOf)
			throws ServiceException {
		String tenantId = StringUtils.trimToNull(filters.get("tenantId"));
		if (tenantId == null) {
			throw new ServiceException("請選擇 Tenant");
		}
		tenantAccessGuard.requireAccess(tenantId);
		Map<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("tenantId", tenantId);
		StringBuilder where = new StringBuilder(" WHERE TENANT_ID=:tenantId");
		addEquals(where, parameters, filters, "executionId", "EXECUTION_ID");
		addEquals(where, parameters, filters, "actionCode", "ACTION_CODE");
		addEquals(where, parameters, filters, "executionStatus", "EXECUTION_STATUS");
		addEquals(where, parameters, filters, "loginAccount", "LOGIN_ACCOUNT");
		long total = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM fm_data_action_execution_audit" + where,
				parameters, Long.class);
		int size = Integer.parseInt(pageOf.getShowRow());
		int page = Integer.parseInt(pageOf.getSelect());
		parameters.put("limit", size);
		parameters.put("offset", (page - 1) * size);
		String sql = """
				SELECT EXECUTION_ID, TENANT_ID, ACTION_CODE, VERSION_NO, LOGIN_ACCOUNT,
				       EXECUTION_STATUS, ROLLBACK_ONLY, STEP_COUNT,
				       REQUEST_PARAMETER_COUNT, START_TIME, END_TIME, DURATION_MS,
				       ERROR_MESSAGE
				  FROM fm_data_action_execution_audit
				""" + where + " ORDER BY START_TIME DESC LIMIT :limit OFFSET :offset";
		List<FmDataActionExecutionAuditView> rows = jdbcTemplate.query(sql, parameters,
				(rs, rowNum) -> new FmDataActionExecutionAuditView(
					rs.getString("EXECUTION_ID"), rs.getString("TENANT_ID"),
					rs.getString("ACTION_CODE"), rs.getInt("VERSION_NO"),
					rs.getString("LOGIN_ACCOUNT"), rs.getString("EXECUTION_STATUS"),
					rs.getString("ROLLBACK_ONLY"), rs.getInt("STEP_COUNT"),
					rs.getInt("REQUEST_PARAMETER_COUNT"), rs.getTimestamp("START_TIME"),
					rs.getTimestamp("END_TIME"), rs.getLong("DURATION_MS"),
					rs.getString("ERROR_MESSAGE")));
		return new AuditPage(rows, total);
	}

	private void addEquals(StringBuilder where, Map<String, Object> parameters,
			Map<String, String> filters, String field, String column) {
		String value = StringUtils.trimToNull(filters.get(field));
		if (value != null) {
			where.append(" AND ").append(column).append("=:").append(field);
			parameters.put(field, value);
		}
	}

	public record AuditPage(List<FmDataActionExecutionAuditView> rows, long total) { }
}
