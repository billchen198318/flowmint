package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FmDataActionExecutorColumnNameTest {

    @Test
    void preservesExistingLowerCamelAliases() {
        assertEquals("displayName", FmDataActionExecutor.toLowerCamel("displayName"));
        assertEquals("assignmentId", FmDataActionExecutor.toLowerCamel("assignmentId"));
        assertEquals("isPrimary", FmDataActionExecutor.toLowerCamel("isPrimary"));
    }

    @Test
    void convertsDatabaseColumnNames() {
        assertEquals("displayName", FmDataActionExecutor.toLowerCamel("DISPLAY_NAME"));
        assertEquals("employeeId", FmDataActionExecutor.toLowerCamel("employee_id"));
        assertEquals("oid", FmDataActionExecutor.toLowerCamel("OID"));
    }
}
