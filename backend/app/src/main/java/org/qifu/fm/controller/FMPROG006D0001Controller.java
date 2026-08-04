package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ControllerException;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.CheckControllerFieldHandler;
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
			validatePool(result, command, create);
			DefaultResult<FmDataSourcePoolView> data = create
					? poolLogicService.create(command) : poolLogicService.update(command);
			setDefaultResponseJsonResult(data, result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private void validatePool(DefaultControllerJsonResultObj<FmDataSourcePoolView> result,
			FmDataSourcePoolCommand command, boolean create)
			throws ControllerException, ServiceException {
		@SuppressWarnings("rawtypes")
		CheckControllerFieldHandler check = getCheckControllerFieldHandler(result);
		check.testField("tenantId", command,
				"@org.apache.commons.lang3.StringUtils@isBlank(tenantId)", "請選擇 Tenant")
				.testField("poolCode", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(poolCode)", "請輸入連線池代碼")
				.testField("poolName", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(poolName)", "請輸入連線池名稱")
				.testField("dbType", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(dbType)", "請選擇資料庫類型")
				.testField("jdbcUrl", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(jdbcUrl)", "請輸入 JDBC URL")
				.testField("username", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(username)", "請輸入資料庫帳號");
		if (create) {
			check.testField("password", command,
					"@org.apache.commons.lang3.StringUtils@isBlank(password)", "請輸入資料庫密碼");
		}
		check.testField("maximumPoolSize", command,
				"maximumPoolSize == null || maximumPoolSize < 1 || maximumPoolSize > 100",
				"最大連線數必須介於 1 到 100")
				.testField("minimumIdle", command,
						"minimumIdle == null || minimumIdle < 0 || minimumIdle > maximumPoolSize",
						"最小閒置數不可小於 0 或超過最大連線數")
				.testField("connectionTimeoutMs", command,
						"connectionTimeoutMs == null || connectionTimeoutMs < 250 || connectionTimeoutMs > 120000",
						"連線逾時必須介於 250 到 120000 毫秒")
				.throwHtmlMessage();
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
			validateTestConnection(result, command);
			setDefaultResponseJsonResult(poolLogicService.test(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private void validateTestConnection(DefaultControllerJsonResultObj<FmDataSourceTestView> result,
			FmDataSourcePoolCommand command) throws ControllerException, ServiceException {
		@SuppressWarnings("rawtypes")
		CheckControllerFieldHandler check = getCheckControllerFieldHandler(result);
		check.testField("tenantId", command,
				"@org.apache.commons.lang3.StringUtils@isBlank(tenantId)", "請選擇 Tenant")
				.testField("dbType", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(dbType)", "請選擇資料庫類型")
				.testField("jdbcUrl", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(jdbcUrl)", "請輸入 JDBC URL")
				.testField("username", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(username)", "請輸入資料庫帳號");
		if (command.oid() == null) {
			check.testField("password", command,
					"@org.apache.commons.lang3.StringUtils@isBlank(password)", "請輸入資料庫密碼");
		}
		check.throwHtmlMessage();
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0001Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		setDefaultResponseJsonResult(poolLogicService.tenantOptions(), result);
		return ResponseEntity.ok(result);
	}
}
