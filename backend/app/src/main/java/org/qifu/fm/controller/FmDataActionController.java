package org.qifu.fm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.logic.IFmDataActionLogicService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/fm/data-actions")
public class FmDataActionController extends CoreApiSupport {

	private static final long serialVersionUID = 1L;

	private final transient IFmDataActionLogicService actionLogicService;
	private final transient IFmTenantAccountService tenantAccountService;

	public FmDataActionController(
			IFmDataActionLogicService actionLogicService,
			IFmTenantAccountService tenantAccountService) {
		this.actionLogicService = actionLogicService;
		this.tenantAccountService = tenantAccountService;
	}

	@PostMapping("/{actionCode}/execute")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataActionExecutionView>> execute(
			@PathVariable String actionCode,
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestHeader(value = "X-FlowMint-Action-Version",
					required = false) Integer versionNo,
			@RequestBody(required = false) Map<String, Object> request) {
		DefaultControllerJsonResultObj<FmDataActionExecutionView> result =
				initDefaultJsonResult();
		try {
			String account = UserUtils.getCurrentUser().getUsername();
			validateTenantMembership(tenantId, account);
			setDefaultResponseJsonResult(actionLogicService.execute(
					tenantId, actionCode, versionNo,
					request == null ? new HashMap<>() : request, account), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> options(
			@RequestHeader("X-FlowMint-Tenant") String tenantId) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result =
				initDefaultJsonResult();
		try {
			String account = UserUtils.getCurrentUser().getUsername();
			validateTenantMembership(tenantId, account);
			setDefaultResponseJsonResult(
					actionLogicService.publishedOptions(tenantId), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/{actionCode}/metadata")
	public ResponseEntity<DefaultControllerJsonResultObj<Map<String, Object>>> metadata(
			@PathVariable String actionCode,
			@RequestHeader("X-FlowMint-Tenant") String tenantId) {
		DefaultControllerJsonResultObj<Map<String, Object>> result =
				initDefaultJsonResult();
		try {
			String account = UserUtils.getCurrentUser().getUsername();
			validateTenantMembership(tenantId, account);
			setDefaultResponseJsonResult(
					actionLogicService.metadata(tenantId, actionCode), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private void validateTenantMembership(String tenantId, String account)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("account", account);
		params.put("status", "ACTIVE");
		List<FmTenantAccount> memberships = Objects.requireNonNullElse(
				tenantAccountService.selectListByParams(
						params, "ACCOUNT", "ASC").getValue(),
				List.of());
		FmTenantAccount membership = memberships.stream()
				.findFirst().orElse(null);
		if (membership == null) {
			throw new ServiceException("目前帳號不屬於指定 Tenant");
		}
	}
}
