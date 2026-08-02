package org.qifu.fm.logic.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.entity.TbAccount;
import org.qifu.core.service.IAccountService;
import org.qifu.fm.dto.command.FmEmployeeCommand;
import org.qifu.fm.dto.view.FmEmployeeView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.logic.IFmEmployeeLogicService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmEmployeeLogicServiceImpl implements IFmEmployeeLogicService {
	private final IFmEmployeeService employeeService;
	private final IFmTenantService tenantService;
	private final IFmTenantAccountService tenantAccountService;
	private final IAccountService<TbAccount, String> accountService;

	public FmEmployeeLogicServiceImpl(IFmEmployeeService employeeService, IFmTenantService tenantService,
			IFmTenantAccountService tenantAccountService, IAccountService<TbAccount, String> accountService) {
		this.employeeService = employeeService;
		this.tenantService = tenantService;
		this.tenantAccountService = tenantAccountService;
		this.accountService = accountService;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmEmployeeView> create(FmEmployeeCommand command) throws ServiceException {
		validateReferences(command.tenantId(), command.account());
		FmEmployee employee = new FmEmployee();
		employee.setTenantId(command.tenantId());
		employee.setEmployeeId(UUID.randomUUID().toString());
		employee.setEmployeeNo(command.employeeNo());
		apply(employee, command);
		DefaultResult<FmEmployee> inserted = employeeService.insert(employee);
		return load(inserted.getValueEmptyThrowMessage().getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmEmployeeView> load(String oid, String message) throws ServiceException {
		FmEmployee employee = requiredEmployee(oid);
		DefaultResult<FmEmployeeView> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(FmEmployeeView.from(employee));
		result.setMessage(message);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmEmployeeView> update(FmEmployeeCommand command) throws ServiceException {
		FmEmployee employee = requiredEmployee(command.oid());
		validateReferences(employee.getTenantId(), command.account());
		apply(employee, command);
		employeeService.update(employee);
		return load(employee.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmEmployeeView> deactivate(String oid) throws ServiceException {
		FmEmployee employee = requiredEmployee(oid);
		employee.setStatus("INACTIVE");
		employeeService.update(employee);
		return load(oid, BaseSystemMessage.updateSuccess());
	}

	@Override
	public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("status", "ACTIVE");
		List<FmOptionView> options = tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue().stream()
				.map(v -> new FmOptionView(v.getTenantId(), v.getTenantCode() + "／" + v.getTenantName())).toList();
		return success(options);
	}

	@Override
	public DefaultResult<List<FmOptionView>> accountOptions(String tenantId) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", "ACTIVE");
		List<FmOptionView> options = tenantAccountService.selectListByParams(params, "ACCOUNT", "ASC").getValue().stream()
				.map(v -> new FmOptionView(v.getAccount(), v.getAccount())).toList();
		return success(options);
	}

	private void validateReferences(String tenantId, String accountId) throws ServiceException {
		Map<String, Object> tenantParams = new HashMap<>();
		tenantParams.put("tenantId", tenantId);
		tenantParams.put("status", "ACTIVE");
		List<FmTenant> tenants = tenantService.selectListByParams(tenantParams, "TENANT_ID", "ASC").getValue();
		if (tenants.isEmpty()) {
			throw new ServiceException("Tenant 不存在或已停用");
		}

		Map<String, Object> linkParams = new HashMap<>();
		linkParams.put("tenantId", tenantId);
		linkParams.put("account", accountId);
		linkParams.put("status", "ACTIVE");
		List<FmTenantAccount> links = tenantAccountService.selectListByParams(linkParams, "ACCOUNT", "ASC").getValue();
		if (links.isEmpty()) {
			throw new ServiceException("帳號未啟用或不屬於此 Tenant");
		}

		TbAccount key = new TbAccount();
		key.setAccount(accountId);
		TbAccount account = accountService.selectByUniqueKey(key).getValueEmptyThrowMessage();
		if (!YesNoKeyProvide.YES.equals(account.getOnJob())) {
			throw new ServiceException("帳號已停用");
		}
	}

	private FmEmployee requiredEmployee(String oid) throws ServiceException {
		if (StringUtils.isBlank(oid)) {
			throw new ServiceException(BaseSystemMessage.parameterBlank());
		}
		return employeeService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
	}

	private void apply(FmEmployee employee, FmEmployeeCommand command) {
		employee.setAccount(command.account());
		employee.setDisplayName(command.displayName());
		employee.setEmail(command.email());
		employee.setMobile(command.mobile());
		employee.setLocale(StringUtils.defaultIfBlank(command.locale(), "zh-TW"));
		employee.setTimezone(StringUtils.defaultIfBlank(command.timezone(), "Asia/Taipei"));
		employee.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		employee.setEffectiveFrom(command.effectiveFrom());
		employee.setEffectiveTo(command.effectiveTo());
		employee.setDescription(command.description());
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
