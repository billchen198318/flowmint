package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.organization.FmApprovalLevelAvailability;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.service.IFmOrgTitleService;

class FmOrgTitleTenantAccessTest {
	@Test
	void loadRejectsOtherTenantBeforeReturningTitle() throws Exception {
		IFmOrgTitleService titles = mock(IFmOrgTitleService.class);
		FmApprovalLevelAvailability available = mock(FmApprovalLevelAvailability.class);
		FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
		FmOrgTitle title = new FmOrgTitle();
		title.setTenantId("OTHER");
		DefaultResult<FmOrgTitle> result = new DefaultResult<>();
		result.setValue(title);
		when(titles.selectByPrimaryKey("TITLE")).thenReturn(result);
		doThrow(new ServiceException("DENIED")).when(access).requireAccess("OTHER");
		FmOrgTitleLogicServiceImpl logic = new FmOrgTitleLogicServiceImpl(titles, available, access);

		assertThrows(ServiceException.class, () -> logic.load("TITLE", null));
		verifyNoInteractions(available);
	}
}
