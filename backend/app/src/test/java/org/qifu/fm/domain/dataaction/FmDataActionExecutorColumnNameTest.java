package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    void rejectsColumnsThatCollideAfterNormalization() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("EMPLOYEE_ID", "E1");
        row.put("employeeId", "E2");

        assertThrows(org.qifu.base.exception.ServiceException.class,
                () -> FmDataActionExecutor.normalizeRows(List.of(row), 100));
    }

    @Test
    void removesOnlyTopLevelOrderByForCountQuery() {
        String sql = "SELECT * FROM (SELECT * FROM x ORDER BY code) nested "
                + "WHERE name='ORDER BY kept' ORDER BY created_at DESC";

        assertEquals("SELECT * FROM (SELECT * FROM x ORDER BY code) nested "
                        + "WHERE name='ORDER BY kept'",
                FmDataActionExecutor.stripTopLevelOrderBy(sql));
    }
}
