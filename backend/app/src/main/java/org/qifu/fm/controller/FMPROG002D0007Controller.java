package org.qifu.fm.controller;

import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.view.FmOrganizationHealthView;
import org.qifu.fm.logic.IFmOrganizationHealthLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG002D0007")
public class FMPROG002D0007Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;
	private final transient IFmOrganizationHealthLogicService organizationHealthLogicService;

	public FMPROG002D0007Controller(
			IFmOrganizationHealthLogicService organizationHealthLogicService) {
		this.organizationHealthLogicService = organizationHealthLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0007Q", check = true)
	@PostMapping("/inspect")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrganizationHealthView>> inspect(
			@RequestHeader("X-FlowMint-Tenant") String tenantId) {
		DefaultControllerJsonResultObj<FmOrganizationHealthView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(organizationHealthLogicService.inspect(tenantId), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

}
