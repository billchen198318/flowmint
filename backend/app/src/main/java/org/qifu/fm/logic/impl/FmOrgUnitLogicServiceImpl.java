package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.dto.command.FmOrgUnitCommand;
import org.qifu.fm.dto.command.FmOrgUnitMoveCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgUnitMovePreviewView;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmOrgUnit;
import org.qifu.fm.entity.FmOrgUnitVersion;
import org.qifu.fm.logic.IFmOrgUnitLogicService;
import org.qifu.fm.service.IFmOrgUnitService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmOrgUnitLogicServiceImpl implements IFmOrgUnitLogicService {
	private static final String ACTIVE = "ACTIVE";
	private static final String INACTIVE = "INACTIVE";

	private final IFmOrgUnitService orgUnitService;
	private final IFmOrgUnitVersionService orgUnitVersionService;
	private final IFmTenantService tenantService;

	public FmOrgUnitLogicServiceImpl(IFmOrgUnitService orgUnitService,
			IFmOrgUnitVersionService orgUnitVersionService, IFmTenantService tenantService) {
		this.orgUnitService = orgUnitService;
		this.orgUnitVersionService = orgUnitVersionService;
		this.tenantService = tenantService;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgUnitView> create(FmOrgUnitCommand command) throws ServiceException {
		validateCommand(command);
		validateTenant(command.tenantId());
		validateUnitCode(command.tenantId(), command.unitCode(), null);

		List<FmOrgUnitView> currentTree = currentTree(command.tenantId(), true);
		FmOrgUnitView parent = resolveParent(currentTree, command.parentOrgUnitId());
		if (parent == null && hasRoot(currentTree)) {
			throw new ServiceException("每個 Tenant 只能有一個根部門");
		}

		FmOrgUnit orgUnit = new FmOrgUnit();
		orgUnit.setTenantId(command.tenantId());
		orgUnit.setOrgUnitId(UUID.randomUUID().toString());
		orgUnit.setUnitCode(command.unitCode());
		orgUnit.setCurrentVersionNo(1);
		orgUnitService.insert(orgUnit).getValueEmptyThrowMessage();

		FmOrgUnitVersion version = new FmOrgUnitVersion();
		version.setTenantId(orgUnit.getTenantId());
		version.setOrgUnitId(orgUnit.getOrgUnitId());
		version.setVersionNo(1);
		applyCommand(version, command);
		applyTreePosition(version, parent, orgUnit.getOrgUnitId());
		orgUnitVersionService.insert(version).getValueEmptyThrowMessage();
		return load(orgUnit.getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmOrgUnitView> load(String oid, String message) throws ServiceException {
		FmOrgUnit orgUnit = requiredOrgUnit(oid);
		FmOrgUnitView view = requiredCurrentView(orgUnit.getTenantId(), orgUnit.getOrgUnitId());
		return success(view, message);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgUnitView> update(FmOrgUnitCommand command) throws ServiceException {
		validateCommand(command);
		FmOrgUnit orgUnit = requiredOrgUnit(command.oid());
		assertVersion(orgUnit, command.currentVersionNo());
		FmOrgUnitView current = requiredCurrentView(orgUnit.getTenantId(), orgUnit.getOrgUnitId());
		if (!Objects.equals(current.getParentOrgUnitId(), normalizeId(command.parentOrgUnitId()))) {
			throw new ServiceException("父部門異動請使用組織樹移動功能");
		}
		FmOrgUnitVersion oldVersion = requiredVersion(current.getVersionOid());
		Date versionTime = nextVersionTime(oldVersion);
		closeVersion(oldVersion, versionTime);

		orgUnit.setCurrentVersionNo(orgUnit.getCurrentVersionNo() + 1);
		orgUnitService.update(orgUnit);

		FmOrgUnitVersion newVersion = copyVersion(oldVersion, orgUnit.getCurrentVersionNo(), versionTime);
		applyCommand(newVersion, command);
		newVersion.setEffectiveFrom(versionTime);
		newVersion.setTreeDepth(current.getTreeDepth());
		newVersion.setPath(current.getPath());
		orgUnitVersionService.insert(newVersion);
		return load(orgUnit.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmOrgUnitView> deactivate(String oid, Integer currentVersionNo) throws ServiceException {
		FmOrgUnit orgUnit = requiredOrgUnit(oid);
		assertVersion(orgUnit, currentVersionNo);
		FmOrgUnitView current = requiredCurrentView(orgUnit.getTenantId(), orgUnit.getOrgUnitId());
		boolean hasActiveChild = currentTree(orgUnit.getTenantId(), false).stream()
				.anyMatch(item -> orgUnit.getOrgUnitId().equals(item.getParentOrgUnitId()));
		if (hasActiveChild) {
			throw new ServiceException("部門仍有啟用中的子部門，不可停用");
		}

		FmOrgUnitVersion oldVersion = requiredVersion(current.getVersionOid());
		Date versionTime = nextVersionTime(oldVersion);
		closeVersion(oldVersion, versionTime);
		orgUnit.setCurrentVersionNo(orgUnit.getCurrentVersionNo() + 1);
		orgUnitService.update(orgUnit);

		FmOrgUnitVersion inactiveVersion = copyVersion(oldVersion, orgUnit.getCurrentVersionNo(), versionTime);
		inactiveVersion.setStatus(INACTIVE);
		inactiveVersion.setEffectiveTo(null);
		orgUnitVersionService.insert(inactiveVersion);
		return load(orgUnit.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	public DefaultResult<List<FmOrgUnitView>> tree(String tenantId, boolean includeInactive) throws ServiceException {
		if (StringUtils.isBlank(tenantId)) {
			throw new ServiceException("請選擇 Tenant");
		}
		return success(currentTree(tenantId, includeInactive), null);
	}

	@Override
	public DefaultResult<FmOrgUnitMovePreviewView> previewMove(FmOrgUnitMoveCommand command) throws ServiceException {
		MoveContext context = validateMove(command);
		int depthDelta = context.newDepth() - context.source().getTreeDepth();
		List<String> warnings = new ArrayList<>();
		if (depthDelta != 0) {
			warnings.add("受影響子樹深度將調整 " + depthDelta);
		}
		FmOrgUnitMovePreviewView preview = new FmOrgUnitMovePreviewView(
				context.source().getOrgUnitId(),
				context.source().getUnitName(),
				context.source().getParentOrgUnitId(),
				command.newParentOrgUnitId(),
				context.source().getTreeDepth(),
				context.newDepth(),
				context.subtree().size(),
				warnings);
		return success(preview, null);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<List<FmOrgUnitView>> move(FmOrgUnitMoveCommand command) throws ServiceException {
		MoveContext context = validateMove(command);
		String newRootPath = context.parent() == null
				? "/" + context.source().getOrgUnitId() + "/"
				: context.parent().getPath() + context.source().getOrgUnitId() + "/";
		int depthDelta = context.newDepth() - context.source().getTreeDepth();
		Map<String, FmOrgUnit> units = orgUnitsById(command.tenantId());

		for (FmOrgUnitView current : context.subtree()) {
			FmOrgUnit orgUnit = units.get(current.getOrgUnitId());
			if (orgUnit == null) {
				throw new ServiceException("找不到子樹部門主檔：" + current.getUnitName());
			}
			FmOrgUnitVersion oldVersion = requiredVersion(current.getVersionOid());
			Date versionTime = nextVersionTime(oldVersion);
			closeVersion(oldVersion, versionTime);
			orgUnit.setCurrentVersionNo(orgUnit.getCurrentVersionNo() + 1);
			orgUnitService.update(orgUnit);

			FmOrgUnitVersion newVersion = copyVersion(oldVersion, orgUnit.getCurrentVersionNo(), versionTime);
			String relativePath = current.getPath().substring(context.source().getPath().length());
			newVersion.setPath(newRootPath + relativePath);
			newVersion.setTreeDepth(current.getTreeDepth() + depthDelta);
			if (current.getOrgUnitId().equals(context.source().getOrgUnitId())) {
				newVersion.setParentOrgUnitId(normalizeId(command.newParentOrgUnitId()));
				newVersion.setSortNo(command.sortNo() == null ? current.getSortNo() : command.sortNo());
			}
			orgUnitVersionService.insert(newVersion);
		}
		return success(currentTree(command.tenantId(), true), BaseSystemMessage.updateSuccess());
	}

	@Override
	public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("status", ACTIVE);
		List<FmOptionView> options = tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue().stream()
				.map(tenant -> new FmOptionView(
						tenant.getTenantId(), tenant.getTenantCode() + "／" + tenant.getTenantName()))
				.toList();
		return success(options, null);
	}

	private void validateCommand(FmOrgUnitCommand command) throws ServiceException {
		if (command == null || StringUtils.isAnyBlank(command.tenantId(), command.unitCode(), command.unitName())) {
			throw new ServiceException(BaseSystemMessage.parameterBlank());
		}
		if (command.effectiveFrom() == null) {
			throw new ServiceException("請輸入生效時間");
		}
		if (command.effectiveTo() != null && !command.effectiveTo().after(command.effectiveFrom())) {
			throw new ServiceException("失效時間必須晚於生效時間");
		}
	}

	private void validateTenant(String tenantId) throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("status", ACTIVE);
		if (tenantService.selectListByParams(params, "TENANT_ID", "ASC").getValue().isEmpty()) {
			throw new ServiceException("Tenant 不存在或已停用");
		}
	}

	private void validateUnitCode(String tenantId, String unitCode, String excludedOrgUnitId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("unitCode", unitCode);
		boolean duplicated = orgUnitService.selectListByParams(params, "UNIT_CODE", "ASC").getValue().stream()
				.anyMatch(item -> !item.getOrgUnitId().equals(excludedOrgUnitId));
		if (duplicated) {
			throw new ServiceException("部門代碼在 Tenant 內不可重複");
		}
	}

	private FmOrgUnit requiredOrgUnit(String oid) throws ServiceException {
		if (StringUtils.isBlank(oid)) {
			throw new ServiceException(BaseSystemMessage.parameterBlank());
		}
		return orgUnitService.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
	}

	private FmOrgUnitVersion requiredVersion(String versionOid) throws ServiceException {
		return orgUnitVersionService.selectByPrimaryKey(versionOid).getValueEmptyThrowMessage();
	}

	private FmOrgUnitView requiredCurrentView(String tenantId, String orgUnitId) throws ServiceException {
		return currentTree(tenantId, true).stream()
				.filter(item -> orgUnitId.equals(item.getOrgUnitId()))
				.findFirst()
				.orElseThrow(() -> new ServiceException("找不到部門目前版本"));
	}

	private List<FmOrgUnitView> currentTree(String tenantId, boolean includeInactive) {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		if (!includeInactive) {
			params.put("status", ACTIVE);
		}
		return orgUnitVersionService.selectCurrentTree(params).getValue();
	}

	private Map<String, FmOrgUnit> orgUnitsById(String tenantId) {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		return orgUnitService.selectListByParams(params, "ORG_UNIT_ID", "ASC").getValue().stream()
				.collect(Collectors.toMap(FmOrgUnit::getOrgUnitId, Function.identity()));
	}

	private FmOrgUnitView resolveParent(List<FmOrgUnitView> tree, String parentOrgUnitId) throws ServiceException {
		if (StringUtils.isBlank(parentOrgUnitId)) {
			return null;
		}
		FmOrgUnitView parent = tree.stream()
				.filter(item -> parentOrgUnitId.equals(item.getOrgUnitId()))
				.findFirst()
				.orElseThrow(() -> new ServiceException("父部門不存在"));
		if (!ACTIVE.equals(parent.getStatus())) {
			throw new ServiceException("父部門已停用");
		}
		return parent;
	}

	private boolean hasRoot(List<FmOrgUnitView> tree) {
		return tree.stream().anyMatch(item -> item.getParentOrgUnitId() == null && ACTIVE.equals(item.getStatus()));
	}

	private void assertVersion(FmOrgUnit orgUnit, Integer expectedVersionNo) throws ServiceException {
		if (expectedVersionNo == null || !expectedVersionNo.equals(orgUnit.getCurrentVersionNo())) {
			throw new ServiceException("部門資料已被異動，請重新載入後再操作");
		}
	}

	private void applyCommand(FmOrgUnitVersion version, FmOrgUnitCommand command) {
		version.setParentOrgUnitId(normalizeId(command.parentOrgUnitId()));
		version.setUnitName(command.unitName());
		version.setShortName(command.shortName());
		version.setUnitType(StringUtils.defaultIfBlank(command.unitType(), "DEPARTMENT"));
		version.setSortNo(command.sortNo() == null ? 0 : command.sortNo());
		version.setIsVirtual(StringUtils.defaultIfBlank(command.isVirtual(), "N"));
		version.setStatus(StringUtils.defaultIfBlank(command.status(), ACTIVE));
		version.setEffectiveFrom(command.effectiveFrom());
		version.setEffectiveTo(command.effectiveTo());
		version.setDescription(command.description());
	}

	private void applyTreePosition(FmOrgUnitVersion version, FmOrgUnitView parent, String orgUnitId) {
		if (parent == null) {
			version.setParentOrgUnitId(null);
			version.setTreeDepth(0);
			version.setPath("/" + orgUnitId + "/");
			return;
		}
		version.setParentOrgUnitId(parent.getOrgUnitId());
		version.setTreeDepth(parent.getTreeDepth() + 1);
		version.setPath(parent.getPath() + orgUnitId + "/");
	}

	private Date nextVersionTime(FmOrgUnitVersion oldVersion) {
		Date now = new Date();
		if (!now.after(oldVersion.getEffectiveFrom())) {
			return new Date(oldVersion.getEffectiveFrom().getTime() + 1L);
		}
		return now;
	}

	private void closeVersion(FmOrgUnitVersion oldVersion, Date versionTime) {
		oldVersion.setEffectiveTo(versionTime);
		orgUnitVersionService.update(oldVersion);
	}

	private FmOrgUnitVersion copyVersion(FmOrgUnitVersion source, Integer versionNo, Date effectiveFrom) {
		FmOrgUnitVersion target = new FmOrgUnitVersion();
		target.setTenantId(source.getTenantId());
		target.setOrgUnitId(source.getOrgUnitId());
		target.setVersionNo(versionNo);
		target.setParentOrgUnitId(source.getParentOrgUnitId());
		target.setUnitName(source.getUnitName());
		target.setShortName(source.getShortName());
		target.setUnitType(source.getUnitType());
		target.setTreeDepth(source.getTreeDepth());
		target.setPath(source.getPath());
		target.setSortNo(source.getSortNo());
		target.setIsVirtual(source.getIsVirtual());
		target.setStatus(source.getStatus());
		target.setEffectiveFrom(effectiveFrom);
		target.setEffectiveTo(null);
		target.setDescription(source.getDescription());
		return target;
	}

	private MoveContext validateMove(FmOrgUnitMoveCommand command) throws ServiceException {
		if (command == null || StringUtils.isAnyBlank(command.tenantId(), command.orgUnitId())) {
			throw new ServiceException(BaseSystemMessage.parameterBlank());
		}
		List<FmOrgUnitView> tree = currentTree(command.tenantId(), false);
		FmOrgUnitView source = tree.stream()
				.filter(item -> command.orgUnitId().equals(item.getOrgUnitId()))
				.findFirst()
				.orElseThrow(() -> new ServiceException("要移動的部門不存在或已停用"));
		FmOrgUnit unit = orgUnitsById(command.tenantId()).get(source.getOrgUnitId());
		if (unit == null) {
			throw new ServiceException("找不到部門主檔");
		}
		assertVersion(unit, command.currentVersionNo());

		FmOrgUnitView parent = resolveParent(tree, command.newParentOrgUnitId());
		if (parent == null && source.getParentOrgUnitId() != null) {
			throw new ServiceException("不可將非根部門移到根層；每個 Tenant 只能有一個根部門");
		}
		if (parent != null && parent.getPath().startsWith(source.getPath())) {
			throw new ServiceException("不可將部門移到自己或其子部門下");
		}
		int newDepth = parent == null ? 0 : parent.getTreeDepth() + 1;
		List<FmOrgUnitView> subtree = tree.stream()
				.filter(item -> item.getPath().startsWith(source.getPath()))
				.sorted(Comparator.comparing(FmOrgUnitView::getTreeDepth))
				.toList();
		return new MoveContext(source, parent, subtree, newDepth);
	}

	private String normalizeId(String value) {
		return StringUtils.isBlank(value) ? null : value.trim();
	}

	private <T> DefaultResult<T> success(T value, String message) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		result.setMessage(message);
		return result;
	}

	private record MoveContext(
			FmOrgUnitView source,
			FmOrgUnitView parent,
			List<FmOrgUnitView> subtree,
			int newDepth) {
	}
}
