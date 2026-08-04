package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmDataSourcePoolCommand;
import org.qifu.fm.dto.view.FmDataSourcePoolView;
import org.qifu.fm.dto.view.FmDataSourceTestView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.logic.IFmDataSourcePoolLogicService;
import org.qifu.fm.service.IFmDataSourcePoolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG006D0001")
public class FMPROG006D0001Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;
	private final transient IFmDataSourcePoolService poolService;
	private final transient IFmDataSourcePoolLogicService poolLogicService;

	public FMPROG006D0001Controller(IFmDataSourcePoolService poolService,
			IFmDataSourcePoolLogicService poolLogicService) {
		this.poolService = poolService;
		this.poolLogicService = poolLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0001Q", check = true)
	@PostMapping("/findPage")
	public ResponseEntity<QueryResult<List<FmDataSourcePoolView>>> findPage(
			@RequestBody SearchBody body) {
		QueryResult<List<FmDataSourcePoolView>> result = initResult();
		try {
			QueryResult<List<FmDataSourcePool>> query = poolService.findPage(
					queryParameter(body).fullEquals("tenantId").value(),
					body.getPageOf().orderBy("POOL_CODE").sortTypeAsc());
			QueryResult<List<FmDataSourcePoolView>> view = new QueryResult<>();
			view.setValue(query.getValue().stream().map(poolLogicService::view).toList());
			view.setMessage(query.getMessage());
			setQueryResponseJsonResult(view, result, body.getPageOf());
		} catch (Exception exception) {
			noSuccessResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0001C", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataSourcePoolView>> save(
			@RequestBody FmDataSourcePoolCommand command) {
		return command(command, true);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0001U", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataSourcePoolView>> update(
			@RequestBody FmDataSourcePoolCommand command) {
		return command(command, false);
	}

	private ResponseEntity<DefaultControllerJsonResultObj<FmDataSourcePoolView>> command(
			FmDataSourcePoolCommand command, boolean create) {
		DefaultControllerJsonResultObj<FmDataSourcePoolView> result = initDefaultJsonResult();
		try {
			DefaultResult<FmDataSourcePoolView> data = create
					? poolLogicService.create(command) : poolLogicService.update(command);
			setDefaultResponseJsonResult(data, result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0001E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataSourcePoolView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmDataSourcePoolView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(poolLogicService.load(body.get("oid"),
					BaseSystemMessage.dataIsExist()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0001D", check = true)
	@PostMapping("/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataSourcePoolView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmDataSourcePoolView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(poolLogicService.deactivate(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0001X", check = true)
	@PostMapping("/test-connection")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataSourceTestView>> testConnection(
			@RequestBody FmDataSourcePoolCommand command) {
		DefaultControllerJsonResultObj<FmDataSourceTestView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(poolLogicService.test(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0001Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		setDefaultResponseJsonResult(poolLogicService.tenantOptions(), result);
		return ResponseEntity.ok(result);
	}
}
