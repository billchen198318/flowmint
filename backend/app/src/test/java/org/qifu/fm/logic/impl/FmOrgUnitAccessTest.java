package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.service.IFmOrgUnitService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.qifu.fm.service.IFmTenantService;

class FmOrgUnitAccessTest {
	@Test
	void treeRejectsInaccessibleTenantBeforeReadingVersions() throws Exception {
		IFmOrgUnitVersionService versions = mock(IFmOrgUnitVersionService.class);
		FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
		doThrow(new ServiceException("DENIED")).when(access).requireAccess("T-OTHER");
		FmOrgUnitLogicServiceImpl logic = new FmOrgUnitLogicServiceImpl(
				mock(IFmOrgUnitService.class), versions, mock(IFmTenantService.class), access);

		assertThrows(ServiceException.class, () -> logic.tree("T-OTHER", false));
		verifyNoInteractions(versions);
	}
}
