package org.qifu.fm.controller;

import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.util.CoreApiSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG010D0003")
public class FMPROG010D0003Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;

	@ControllerMethodAuthority(programId = "FM_PROG010D0003Q", check = true)
	@PostMapping("/access")
	public ResponseEntity<DefaultControllerJsonResultObj<Boolean>> access() {
		DefaultControllerJsonResultObj<Boolean> result = initDefaultJsonResult();
		DefaultResult<Boolean> data = new DefaultResult<>();
		data.setSuccess("Y");
		data.setValue(Boolean.TRUE);
		setDefaultResponseJsonResult(data, result);
		return ResponseEntity.ok(result);
	}
}
