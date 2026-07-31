package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.dto.command.FmOrgLevelCommand;
import org.qifu.fm.dto.command.FmOrgLevelSchemeCommand;
import org.qifu.fm.dto.view.FmOrgLevelSchemeView;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgLevelScheme;
import org.qifu.fm.logic.IFmOrgLevelSchemeLogicService;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgLevelSchemeService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmOrgLevelSchemeLogicServiceImpl implements IFmOrgLevelSchemeLogicService {

	private final IFmOrgLevelSchemeService schemes;
	private final IFmOrgApprovalLevelService levels;
	private final IFmTenantService tenants;

	public FmOrgLevelSchemeLogicServiceImpl(IFmOrgLevelSchemeService s, IFmOrgApprovalLevelService l,
			IFmTenantService t) {
		schemes = s;
		levels = l;
		tenants = t;
	}

	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgLevelSchemeView> create(FmOrgLevelSchemeCommand c) throws ServiceException {
		validate(c);
		requireTenant(c.tenantId());
		FmOrgLevelScheme s = new FmOrgLevelScheme();
		s.setTenantId(c.tenantId());
		s.setLevelSchemeId(UUID.randomUUID().toString());
		apply(s, c);
		schemes.insert(s);
		for (FmOrgLevelCommand x : c.levels())
			levels.insert(newLevel(s, x));
		return load(s.getOid(), BaseSystemMessage.insertSuccess());
	}

	public DefaultResult<FmOrgLevelSchemeView> load(String oid, String message) throws ServiceException {
		FmOrgLevelScheme s = schemes.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		Map<String, Object> p = new HashMap<>();
		p.put("tenantId", s.getTenantId());
		p.put("levelSchemeId", s.getLevelSchemeId());
		List<FmOrgApprovalLevel> l = levels.selectListByParams(p, "LEVEL_ORDER", "ASC").getValue();
		DefaultResult<FmOrgLevelSchemeView> r = new DefaultResult<>();
		r.setSuccess(YesNoKeyProvide.YES);
		r.setValue(FmOrgLevelSchemeView.from(s, l));
		r.setMessage(message);
		return r;
	}

	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgLevelSchemeView> update(FmOrgLevelSchemeCommand c) throws ServiceException {
		validate(c);
		FmOrgLevelScheme s = schemes.selectByPrimaryKey(c.oid()).getValueEmptyThrowMessage();
		apply(s, c);
		schemes.update(s);
		Map<String, Object> p = new HashMap<>();
		p.put("tenantId", s.getTenantId());
		p.put("levelSchemeId", s.getLevelSchemeId());
		Map<String, FmOrgApprovalLevel> old = levels.selectListByParams(p, "LEVEL_ORDER", "ASC").getValue().stream()
				.collect(Collectors.toMap(FmOrgApprovalLevel::getOid, v -> v));
		Set<String> seen = new HashSet<>();
		for (FmOrgLevelCommand x : c.levels()) {
			if (StringUtils.isBlank(x.oid()))
				levels.insert(newLevel(s, x));
			else {
				FmOrgApprovalLevel v = old.get(x.oid());
				if (v == null)
					throw new ServiceException(BaseSystemMessage.parameterIncorrect());
				apply(v, x);
				levels.update(v);
				seen.add(v.getOid());
			}
		}
		Date now = new Date();
		for (FmOrgApprovalLevel v : old.values())
			if (!seen.contains(v.getOid())) {
				v.setStatus("INACTIVE");
				if (v.getEffectiveTo() == null || v.getEffectiveTo().after(now))
					v.setEffectiveTo(now);
				levels.update(v);
			}
		return load(s.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgLevelSchemeView> deactivate(String oid) throws ServiceException {
		FmOrgLevelScheme s = schemes.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
		s.setStatus("INACTIVE");
		schemes.update(s);
		return load(oid, BaseSystemMessage.updateSuccess());
	}

	private void validate(FmOrgLevelSchemeCommand c) throws ServiceException {
		if (c.levels() == null || c.levels().isEmpty())
			throw new ServiceException("至少需要一個組織簽核層級");
		Set<String> codes = new HashSet<>();
		Set<Integer> orders = new HashSet<>();
		long highest = 0;
		for (FmOrgLevelCommand x : c.levels()) {
			if (StringUtils.isAnyBlank(x.levelCode(), x.levelName()) || x.levelOrder() == null || x.levelOrder() < 0)
				throw new ServiceException("層級代碼、名稱與順序不正確");
			if (!codes.add(x.levelCode()) || !orders.add(x.levelOrder()))
				throw new ServiceException("層級代碼與順序不可重複");
			if ("Y".equals(x.isHighestLevel()) && "ACTIVE".equals(StringUtils.defaultIfBlank(x.status(), "ACTIVE")))
				highest++;
			if (x.effectiveFrom() == null || (x.effectiveTo() != null && !x.effectiveTo().after(x.effectiveFrom())))
				throw new ServiceException("層級有效期間不正確");
		}
		if (highest != 1)
			throw new ServiceException("有效層級必須且只能有一個最高層級");
		if (c.effectiveFrom() == null || (c.effectiveTo() != null && !c.effectiveTo().after(c.effectiveFrom())))
			throw new ServiceException("方案有效期間不正確");
	}

	private void requireTenant(String id) throws ServiceException {
		Map<String, Object> p = new HashMap<>();
		p.put("tenantId", id);
		p.put("status", "ACTIVE");
		if (tenants.selectListByParams(p, "TENANT_ID", "ASC").getValue().isEmpty())
			throw new ServiceException("Tenant 不存在或已停用");
	}

	private void apply(FmOrgLevelScheme s, FmOrgLevelSchemeCommand c) {
		s.setSchemeCode(c.schemeCode());
		s.setSchemeName(c.schemeName());
		s.setIsDefault(StringUtils.defaultIfBlank(c.isDefault(), "N"));
		s.setStatus(StringUtils.defaultIfBlank(c.status(), "ACTIVE"));
		s.setEffectiveFrom(c.effectiveFrom());
		s.setEffectiveTo(c.effectiveTo());
		s.setDescription(c.description());
	}

	private FmOrgApprovalLevel newLevel(FmOrgLevelScheme s, FmOrgLevelCommand c) {
		FmOrgApprovalLevel v = new FmOrgApprovalLevel();
		v.setTenantId(s.getTenantId());
		v.setApprovalLevelId(UUID.randomUUID().toString());
		v.setLevelSchemeId(s.getLevelSchemeId());
		apply(v, c);
		return v;
	}

	private void apply(FmOrgApprovalLevel v, FmOrgLevelCommand c) {
		v.setLevelCode(c.levelCode());
		v.setLevelName(c.levelName());
		v.setLevelOrder(c.levelOrder());
		v.setIsHighestLevel(StringUtils.defaultIfBlank(c.isHighestLevel(), "N"));
		v.setStatus(StringUtils.defaultIfBlank(c.status(), "ACTIVE"));
		v.setEffectiveFrom(c.effectiveFrom());
		v.setEffectiveTo(c.effectiveTo());
		v.setDescription(c.description());
	}
}
