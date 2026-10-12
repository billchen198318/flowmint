package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FmExternalOrganizationPaginationTest {
    @Test
    void veryLargePageStartsAfterAvailableRows() {
        assertEquals(141, FmExternalOrganizationQueryService.pageStart(
                Integer.MAX_VALUE, 200, 141));
        assertEquals(35, FmExternalOrganizationQueryService.pageStart(
                Integer.MAX_VALUE, 200, 35));
        assertEquals(200, FmExternalOrganizationQueryService.pageStart(2, 200, 450));
    }
}
