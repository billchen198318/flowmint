package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ControllerException;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.CheckControllerFieldHandler;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmAiProviderCommand;
import org.qifu.fm.dto.view.FmAiProviderView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmAiProvider;
import org.qifu.fm.logic.IFmAiProviderLogicService;
import org.qifu.fm.service.IFmAiProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG010D0001")
public class FMPROG010D0001Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;
	private final transient IFmAiProviderService providerService;
	private final transient IFmAiProviderLogicService providerLogicService;
	private final transient FmTenantAccessGuard tenantAccessGuard;

	public FMPROG010D0001Controller(IFmAiProviderService providerService,
			IFmAiProviderLogicService providerLogicService,
			FmTenantAccessGuard tenantAccessGuard) {
		this.providerService = providerService;
		this.providerLogicService = providerLogicService;
		this.tenantAccessGuard = tenantAccessGuard;
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0001Q", check = true)
	@PostMapping("/findPage")
	public ResponseEntity<QueryResult<List<FmAiProviderView>>> findPage(
			@RequestBody SearchBody body) {
		QueryResult<List<FmAiProviderView>> result = initResult();
		try {
			Map<String, Object> parameters = queryParameter(body)
					.fullEquals("tenantId")
					.fullEquals("status")
					.value();
			String tenantId = parameters.get("tenantId") == null
					? null : String.valueOf(parameters.get("tenantId"));
			tenantAccessGuard.requireQueryAccess(tenantId);
			QueryResult<List<FmAiProvider>> query = providerService.findPage(
					parameters,
					body.getPageOf().orderBy("PROVIDER_CODE").sortTypeAsc());
			QueryResult<List<FmAiProviderView>> view = new QueryResult<>();
			List<FmAiProvider> providers = query.getValue() == null
					? List.of() : query.getValue();
			view.setValue(providers.stream()
					.map(this::safeView)
					.toList());
			view.setMessage(query.getMessage());
			setQueryResponseJsonResult(view, result, body.getPageOf());
		} catch (Exception exception) {
			noSuccessResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0001C", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmAiProviderView>> save(
			@RequestBody FmAiProviderCommand command) {
		return command(command, true);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0001E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmAiProviderView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmAiProviderView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(providerLogicService.load(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0001U", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmAiProviderView>> update(
			@RequestBody FmAiProviderCommand command) {
		return command(command, false);
	}

	private ResponseEntity<DefaultControllerJsonResultObj<FmAiProviderView>> command(
			FmAiProviderCommand command, boolean create) {
		DefaultControllerJsonResultObj<FmAiProviderView> result = initDefaultJsonResult();
		try {
			validateProvider(result, command, create);
			DefaultResult<FmAiProviderView> data = create
					? providerLogicService.create(command)
					: providerLogicService.update(command);
			setDefaultResponseJsonResult(data, result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private void validateProvider(
			DefaultControllerJsonResultObj<FmAiProviderView> result,
			FmAiProviderCommand command, boolean create)
			throws ControllerException, ServiceException {
		@SuppressWarnings("rawtypes")
		CheckControllerFieldHandler check = getCheckControllerFieldHandler(result);
		check.testField("tenantId", command,
				"@org.apache.commons.lang3.StringUtils@isBlank(tenantId)",
				"請選擇 Tenant")
				.testField("providerCode", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(providerCode)",
						"請輸入 Provider 代碼")
				.testField("providerCode", command,
						"providerCode != null && providerCode.length() > 50",
						"Provider 代碼不可超過 50 字")
				.testField("providerType", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(providerType)",
						"請選擇 Provider 類型")
				.testField("providerType", command,
						"providerType != null && !@org.apache.commons.lang3.StringUtils@equalsAny(providerType, \"OPENAI\", \"GEMINI\", \"GROQ\", \"OPENROUTER\")",
						"Provider 類型不正確")
				.testField("displayName", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(displayName)",
						"請輸入顯示名稱")
				.testField("displayName", command,
						"displayName != null && displayName.length() > 100",
						"顯示名稱不可超過 100 字")
				.testField("baseUrl", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(baseUrl)",
						"請輸入 Base URL")
				.testField("baseUrl", command,
						"baseUrl != null && baseUrl.length() > 500",
						"Base URL 不可超過 500 字")
				.testField("modelId", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(modelId)",
						"請輸入 Model ID")
				.testField("modelId", command,
						"modelId != null && modelId.length() > 100",
						"Model ID 不可超過 100 字");
		if (create) {
			check.testField("apiKey", command,
					"@org.apache.commons.lang3.StringUtils@isBlank(apiKey)",
					"請輸入 API Key");
		}
		check.testField("temperature", command,
				"temperature == null || temperature.doubleValue() < 0 || temperature.doubleValue() > 2",
				"Temperature 必須介於 0 到 2")
				.testField("maxOutputTokens", command,
						"maxOutputTokens == null || maxOutputTokens < 256 || maxOutputTokens > 32000",
						"最大輸出 Token 必須介於 256 到 32000")
				.testField("timeoutSeconds", command,
						"timeoutSeconds == null || timeoutSeconds < 10 || timeoutSeconds > 120",
						"逾時秒數必須介於 10 到 120")
				.testField("defaultFlag", command,
						"!@org.apache.commons.lang3.StringUtils@equalsAny(defaultFlag, \"Y\", \"N\")",
						"Tenant 預設值不正確")
				.testField("status", command,
						"!@org.apache.commons.lang3.StringUtils@equalsAny(status, \"ACTIVE\", \"INACTIVE\")",
						"狀態不正確")
				.throwHtmlMessage();
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0001D", check = true)
	@PostMapping("/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmAiProviderView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmAiProviderView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					providerLogicService.deactivate(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0001U", check = true)
	@PostMapping("/test-connection")
	public ResponseEntity<DefaultControllerJsonResultObj<FmAiProviderView>>
			testConnection(@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmAiProviderView> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					providerLogicService.testConnection(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0001Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>>
			tenantOptions() {
		DefaultControllerJsonResultObj<List<FmOptionView>> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(providerLogicService.tenantOptions(), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private FmAiProviderView safeView(FmAiProvider provider) {
		try {
			return providerLogicService.view(provider);
		} catch (Exception exception) {
			throw new IllegalStateException("AI Provider API Key 無法讀取", exception);
		}
	}
}
