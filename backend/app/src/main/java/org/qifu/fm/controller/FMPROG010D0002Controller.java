package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmApiClientCommand;
import org.qifu.fm.dto.command.FmApiKeyIssueCommand;
import org.qifu.fm.dto.command.FmApiKeyRevokeCommand;
import org.qifu.fm.dto.view.FmApiClientKeyView;
import org.qifu.fm.dto.view.FmApiClientView;
import org.qifu.fm.dto.view.FmApiKeyIssueView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmApiClient;
import org.qifu.fm.logic.IFmExternalApiManagementLogicService;
import org.qifu.fm.service.IFmApiClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG010D0002")
public class FMPROG010D0002Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;
	private final transient IFmApiClientService clientService;
	private final transient IFmExternalApiManagementLogicService managementService;
	private final transient FmTenantAccessGuard tenantAccessGuard;

	public FMPROG010D0002Controller(IFmApiClientService clientService,
			IFmExternalApiManagementLogicService managementService,
			FmTenantAccessGuard tenantAccessGuard) {
		this.clientService = clientService;
		this.managementService = managementService;
		this.tenantAccessGuard = tenantAccessGuard;
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002Q", check = true)
	@PostMapping("/findPage")
	public ResponseEntity<QueryResult<List<FmApiClientView>>> findPage(
			@RequestBody SearchBody body) {
		QueryResult<List<FmApiClientView>> result = initResult();
		try {
			Map<String, Object> parameters = queryParameter(body)
					.fullEquals("tenantId")
					.fullEquals("systemType")
					.fullEquals("status")
					.fullLink("clientCodeLike")
					.fullLink("clientNameLike")
					.value();
			String tenantId = parameters.get("tenantId") == null
					? null : String.valueOf(parameters.get("tenantId"));
			tenantAccessGuard.requireQueryAccess(tenantId);
			QueryResult<List<FmApiClient>> query = clientService.findPage(parameters,
					body.getPageOf().orderBy("CLIENT_CODE").sortTypeAsc());
			QueryResult<List<FmApiClientView>> views = new QueryResult<>();
			views.setValue(query.getValue().stream().map(this::safeView).toList());
			views.setMessage(query.getMessage());
			setQueryResponseJsonResult(views, result, body.getPageOf());
		} catch (Exception exception) {
			noSuccessResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002A", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApiClientView>> save(
			@RequestBody FmApiClientCommand command) {
		DefaultControllerJsonResultObj<FmApiClientView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(managementService.create(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>>
			tenantOptions() {
		DefaultControllerJsonResultObj<List<FmOptionView>> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(managementService.tenantOptions(), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApiClientView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmApiClientView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(managementService.load(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002E", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApiClientView>> update(
			@RequestBody FmApiClientCommand command) {
		DefaultControllerJsonResultObj<FmApiClientView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(managementService.update(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002E", check = true)
	@PostMapping("/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApiClientView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmApiClientView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					managementService.deactivate(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002E", check = true)
	@PostMapping("/keys")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmApiClientKeyView>>> keys(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<List<FmApiClientKeyView>> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					managementService.keys(body.get("clientOid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002E", check = true)
	@PostMapping("/keys/issue")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApiKeyIssueView>> issueKey(
			@RequestBody FmApiKeyIssueCommand command) {
		return issue(command, false);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002E", check = true)
	@PostMapping("/keys/rotate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApiKeyIssueView>> rotateKey(
			@RequestBody FmApiKeyIssueCommand command) {
		return issue(command, true);
	}

	@ControllerMethodAuthority(programId = "FM_PROG010D0002E", check = true)
	@PostMapping("/keys/revoke")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApiClientKeyView>> revokeKey(
			@RequestBody FmApiKeyRevokeCommand command) {
		DefaultControllerJsonResultObj<FmApiClientKeyView> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(managementService.revokeKey(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private ResponseEntity<DefaultControllerJsonResultObj<FmApiKeyIssueView>> issue(
			FmApiKeyIssueCommand command, boolean rotate) {
		DefaultControllerJsonResultObj<FmApiKeyIssueView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(rotate
					? managementService.rotateKey(command)
					: managementService.issueKey(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private FmApiClientView safeView(FmApiClient client) {
		try {
			return managementService.view(client);
		} catch (Exception exception) {
			throw new IllegalStateException("Cannot read API client policy.", exception);
		}
	}
}
