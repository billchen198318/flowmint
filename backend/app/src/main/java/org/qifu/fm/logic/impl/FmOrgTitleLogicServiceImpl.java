package org.qifu.fm.logic.impl;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.dto.command.FmOrgTitleCommand;
import org.qifu.fm.dto.view.FmOrgTitleView;
import org.qifu.fm.domain.organization.FmApprovalLevelAvailability;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.logic.IFmOrgTitleLogicService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmOrgTitleLogicServiceImpl implements IFmOrgTitleLogicService {
  private final IFmOrgTitleService titles;
  private final FmApprovalLevelAvailability availableLevels;
  private final FmTenantAccessGuard tenantAccessGuard;

  public FmOrgTitleLogicServiceImpl(IFmOrgTitleService titles,
      FmApprovalLevelAvailability availableLevels, FmTenantAccessGuard tenantAccessGuard) {
    this.titles = titles;
    this.availableLevels = availableLevels;
    this.tenantAccessGuard = tenantAccessGuard;
  }

  @Transactional(readOnly = false, rollbackFor = Exception.class)
  public DefaultResult<FmOrgTitleView> create(FmOrgTitleCommand command) throws ServiceException {
    validate(command);
    tenantAccessGuard.requireAccess(command.tenantId());
    requireReferences(command);
    FmOrgTitle value = new FmOrgTitle();
    value.setTenantId(command.tenantId());
    value.setTitleId(UUID.randomUUID().toString());
    value.setTitleCode(command.titleCode());
    apply(value, command);
    titles.insert(value);
    return load(value.getOid(), BaseSystemMessage.insertSuccess());
  }

  public DefaultResult<FmOrgTitleView> load(String oid, String message) throws ServiceException {
    FmOrgTitle value = titles.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
    tenantAccessGuard.requireAccess(value.getTenantId());
    DefaultResult<FmOrgTitleView> result = new DefaultResult<>();
    result.setSuccess(YesNoKeyProvide.YES);
    result.setValue(FmOrgTitleView.from(value));
    result.setMessage(message);
    return result;
  }

  @Transactional(readOnly = false, rollbackFor = Exception.class)
  public DefaultResult<FmOrgTitleView> update(FmOrgTitleCommand command) throws ServiceException {
    validate(command);
    FmOrgTitle value = titles.selectByPrimaryKey(command.oid()).getValueEmptyThrowMessage();
    tenantAccessGuard.requireAccess(value.getTenantId());
    if (!value.getTenantId().equals(command.tenantId())) {
      throw new ServiceException(BaseSystemMessage.parameterIncorrect());
    }
    requireReferences(command);
    apply(value, command);
    titles.update(value);
    return load(value.getOid(), BaseSystemMessage.updateSuccess());
  }

  @Transactional(readOnly = false, rollbackFor = Exception.class)
  public DefaultResult<FmOrgTitleView> deactivate(String oid) throws ServiceException {
    FmOrgTitle value = titles.selectByPrimaryKey(oid).getValueEmptyThrowMessage();
    tenantAccessGuard.requireAccess(value.getTenantId());
    value.setStatus("INACTIVE");
    titles.update(value);
    return load(oid, BaseSystemMessage.updateSuccess());
  }

  private void validate(FmOrgTitleCommand command) throws ServiceException {
    if (StringUtils.isAnyBlank(command.tenantId(), command.titleCode(),
        command.titleName(), command.approvalLevelId())) {
      throw new ServiceException(BaseSystemMessage.parameterIncorrect());
    }
    if (command.effectiveFrom() == null ||
        (command.effectiveTo() != null && !command.effectiveTo().after(command.effectiveFrom()))) {
      throw new ServiceException("職稱有效期間不正確");
    }
  }

  private void requireReferences(FmOrgTitleCommand command) throws ServiceException {
    if (availableLevels.availableDuring(command.tenantId(), command.effectiveFrom(), command.effectiveTo()).stream()
        .noneMatch(value -> value.getApprovalLevelId().equals(command.approvalLevelId()))) {
      throw new ServiceException("簽核 Level 不存在、已停用或不涵蓋職稱有效期間");
    }
  }

  private void apply(FmOrgTitle value, FmOrgTitleCommand command) {
    value.setTitleName(command.titleName());
    value.setApprovalLevelId(command.approvalLevelId());
    value.setIsManagerTitle(StringUtils.defaultIfBlank(command.isManagerTitle(), "N"));
    value.setSortNo(command.sortNo() == null ? 0 : command.sortNo());
    value.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
    value.setEffectiveFrom(command.effectiveFrom());
    value.setEffectiveTo(command.effectiveTo());
    value.setDescription(command.description());
  }
}
