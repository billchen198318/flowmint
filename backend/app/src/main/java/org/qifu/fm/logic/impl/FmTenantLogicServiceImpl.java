package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.entity.TbAccount;
import org.qifu.core.entity.TbUserRole;
import org.qifu.core.service.IAccountService;
import org.qifu.core.service.IUserRoleService;
import org.qifu.fm.dto.command.FmTenantAccountCommand;
import org.qifu.fm.dto.command.FmTenantCommand;
import org.qifu.fm.dto.command.FmResetPasswordCommand;
import org.qifu.fm.dto.view.FmTenantAccountView;
import org.qifu.fm.dto.view.FmTenantView;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.logic.IFmTenantLogicService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmTenantLogicServiceImpl implements IFmTenantLogicService {
	private final IFmTenantService tenantService;
	private final IFmTenantAccountService tenantAccountService;
	private final IAccountService<TbAccount, String> accountService;
	private final IUserRoleService<TbUserRole, String> userRoleService;
	private final PasswordEncoder passwordEncoder;

	public FmTenantLogicServiceImpl(IFmTenantService tenantService, IFmTenantAccountService tenantAccountService,
			IAccountService<TbAccount, String> accountService, IUserRoleService<TbUserRole, String> userRoleService,
			PasswordEncoder passwordEncoder) {
		this.tenantService = tenantService;
		this.tenantAccountService = tenantAccountService;
		this.accountService = accountService;
		this.userRoleService = userRoleService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmTenantView> create(FmTenantCommand command) throws ServiceException {
		FmTenant tenant = toNewEntity(command);
		DefaultResult<FmTenant> result = tenantService.insert(tenant);
		return load(result.getValueEmptyThrowMessage().getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmTenantView> load(String oid, String message) throws ServiceException {
		FmTenant tenant = requiredTenant(oid);
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenant.getTenantId());
		List<FmTenantAccountView> accounts = tenantAccountService.selectListByParams(params, "ACCOUNT", "ASC")
				.getValue().stream().map(FmTenantAccountView::from).toList();
		DefaultResult<FmTenantView> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(FmTenantView.from(tenant, accounts));
		result.setMessage( message );
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmTenantView> update(FmTenantCommand command) throws ServiceException {
		FmTenant tenant = requiredTenant(command.oid());
		tenant.setTenantName(command.tenantName());
		tenant.setDefaultLocale(defaultValue(command.defaultLocale(), "zh-TW"));
		tenant.setDefaultTimezone(defaultValue(command.defaultTimezone(), "Asia/Taipei"));
		tenant.setStatus(defaultValue(command.status(), "ACTIVE"));
		tenant.setDescription(command.description());
		tenantService.update(tenant);
		return load(tenant.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmTenantView> deactivate(String oid) throws ServiceException {
		FmTenant tenant = requiredTenant(oid);
		tenant.setStatus("INACTIVE");
		tenantService.update(tenant);
		return load(oid, BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmTenantView> addAccount(FmTenantAccountCommand command) throws ServiceException {
		FmTenant tenant = requiredTenant(command.tenantOid());
		validateAccountCommand(command);
		TbAccount account = new TbAccount();
		account.setAccount(command.account());
		DefaultResult<TbAccount> existing = accountService.selectByUniqueKey(account);
		if (Boolean.TRUE.equals(command.createNewAccount())) {
			if (existing.getValue() != null) {
				throw new ServiceException(BaseSystemMessage.dataIsExist());
			}
			account.setPassword(passwordEncoder.encode(command.password()));
			account.setOnJob(YesNoKeyProvide.YES);
			accountService.insert(account);
		} else if (existing.getValue() == null) {
			throw new ServiceException(BaseSystemMessage.dataNoExist());
		}
		FmTenantAccount link = new FmTenantAccount();
		link.setTenantId(tenant.getTenantId());
		link.setAccount(command.account());
		link.setIsDefault(defaultValue(command.isDefault(), YesNoKeyProvide.NO));
		link.setStatus(defaultValue(command.status(), "ACTIVE"));
		link.setEffectiveFrom(command.effectiveFrom());
		link.setEffectiveTo(command.effectiveTo());
		tenantAccountService.insert(link);
		return load(tenant.getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmTenantView> updateAccount(FmTenantAccountCommand command) throws ServiceException {
		FmTenant tenant = requiredTenant(command.tenantOid());
		FmTenantAccount link = tenantAccountService.selectByPrimaryKey(command.oid()).getValueEmptyThrowMessage();
		if (!tenant.getTenantId().equals(link.getTenantId())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		link.setIsDefault(defaultValue(command.isDefault(), YesNoKeyProvide.NO));
		link.setStatus(defaultValue(command.status(), "ACTIVE"));
		link.setEffectiveFrom(command.effectiveFrom());
		link.setEffectiveTo(command.effectiveTo());
		tenantAccountService.update(link);
		return load(tenant.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmTenantView> resetPassword(FmResetPasswordCommand command) throws ServiceException {
		FmTenant tenant = requiredTenant(command.tenantOid());
		Map<String, Object> linkParams = new HashMap<>();
		linkParams.put("tenantId", tenant.getTenantId());
		linkParams.put("account", command.account());
		if (tenantAccountService.selectListByParams(linkParams, "ACCOUNT", "ASC").getValue().isEmpty()) {
			throw new ServiceException(BaseSystemMessage.dataNoExist());
		}
		if (StringUtils.isBlank(command.password()) || !command.password().equals(command.confirmPassword())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}

		TbAccount accountKey = new TbAccount();
		accountKey.setAccount(command.account());
		TbAccount account = accountService.selectByUniqueKey(accountKey).getValueEmptyThrowMessage();
		account.setPassword(passwordEncoder.encode(command.password()));
		accountService.update(account);
		return load(tenant.getOid(), BaseSystemMessage.updateSuccess());
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmTenantView> deactivateAccount(String tenantOid, String accountId) throws ServiceException {
		FmTenant tenant = requiredTenant(tenantOid);
		Map<String, Object> tenantLinkParams = new HashMap<>();
		tenantLinkParams.put("tenantId", tenant.getTenantId());
		tenantLinkParams.put("account", accountId);
		if (tenantAccountService.selectListByParams(tenantLinkParams, "ACCOUNT", "ASC").getValue().isEmpty()) {
			throw new ServiceException(BaseSystemMessage.dataNoExist());
		}

		Map<String, Object> roleParams = new HashMap<>();
		roleParams.put("account", accountId);
		roleParams.put("role", "admin");
		if (!userRoleService.selectListByParams(roleParams).getValue().isEmpty()) {
			throw new ServiceException("admin 角色帳號不允許停用");
		}

		TbAccount accountKey = new TbAccount();
		accountKey.setAccount(accountId);
		TbAccount account = accountService.selectByUniqueKey(accountKey).getValueEmptyThrowMessage();
		account.setOnJob(YesNoKeyProvide.NO);
		accountService.update(account);

		Date disabledAt = new Date();
		Map<String, Object> activeLinkParams = new HashMap<>();
		activeLinkParams.put("account", accountId);
		activeLinkParams.put("status", "ACTIVE");
		for (FmTenantAccount link : tenantAccountService
				.selectListByParams(activeLinkParams, "TENANT_ID", "ASC").getValue()) {
			link.setStatus("INACTIVE");
			if (link.getEffectiveTo() == null || link.getEffectiveTo().after(disabledAt)) {
				link.setEffectiveTo(disabledAt);
			}
			tenantAccountService.update(link);
		}
		return load(tenant.getOid(), BaseSystemMessage.updateSuccess());
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmTenantView> activateAccount(String tenantOid, String accountId) throws ServiceException {
		FmTenant tenant = requiredTenant(tenantOid);
		Map<String, Object> tenantLinkParams = new HashMap<>();
		tenantLinkParams.put("tenantId", tenant.getTenantId());
		tenantLinkParams.put("account", accountId);
		if (tenantAccountService.selectListByParams(tenantLinkParams, "ACCOUNT", "ASC").getValue().isEmpty()) {
			throw new ServiceException(BaseSystemMessage.dataNoExist());
		}

		TbAccount accountKey = new TbAccount();
		accountKey.setAccount(accountId);
		TbAccount account = accountService.selectByUniqueKey(accountKey).getValueEmptyThrowMessage();
		account.setOnJob(YesNoKeyProvide.YES);
		accountService.update(account);

		Map<String, Object> inactiveLinkParams = new HashMap<>();
		inactiveLinkParams.put("account", accountId);
		inactiveLinkParams.put("status", "INACTIVE");
		for (FmTenantAccount link : tenantAccountService
				.selectListByParams(inactiveLinkParams, "TENANT_ID", "ASC").getValue()) {
			link.setStatus("ACTIVE");
			link.setEffectiveTo(null);
			tenantAccountService.update(link);
		}
		return load(tenant.getOid(), BaseSystemMessage.updateSuccess());
	}
	private FmTenant requiredTenant(String oid) throws ServiceException {
		if (StringUtils.isBlank(oid)) {
			throw new ServiceException(BaseSystemMessage.parameterBlank());
		}
		return tenantService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
	}

	private FmTenant toNewEntity(FmTenantCommand command) {
		FmTenant tenant = new FmTenant();
		tenant.setTenantId(command.tenantId());
		tenant.setTenantCode(command.tenantCode());
		tenant.setTenantName(command.tenantName());
		tenant.setDefaultLocale(defaultValue(command.defaultLocale(), "zh-TW"));
		tenant.setDefaultTimezone(defaultValue(command.defaultTimezone(), "Asia/Taipei"));
		tenant.setStatus(defaultValue(command.status(), "ACTIVE"));
		tenant.setDescription(command.description());
		return tenant;
	}

	private void validateAccountCommand(FmTenantAccountCommand command) throws ServiceException {
		if (command.effectiveFrom() == null
				|| (command.effectiveTo() != null && !command.effectiveTo().after(command.effectiveFrom()))) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		if (Boolean.TRUE.equals(command.createNewAccount())
				&& (StringUtils.isBlank(command.password()) || !command.password().equals(command.confirmPassword()))) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private String defaultValue(String value, String defaultValue) {
		return StringUtils.defaultIfBlank(value, defaultValue);
	}
}