package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
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
			view.setValue(query.getValue().stream()
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
		DefaultControllerJsonResultObj<FmAiProviderView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(providerLogicService.create(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
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
		DefaultControllerJsonResultObj<FmAiProviderView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(providerLogicService.update(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
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
