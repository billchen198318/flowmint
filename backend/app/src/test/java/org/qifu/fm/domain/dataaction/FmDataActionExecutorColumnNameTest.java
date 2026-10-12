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

    @Test
    void keepsOrderByInsideDoubleQuotedTextAndNestedQueries() {
        String sql = "SELECT \"x ORDER BY y\" AS label FROM "
                + "(SELECT id FROM items ORDER BY id) nested ORDER BY label";
        assertEquals("SELECT \"x ORDER BY y\" AS label FROM "
                        + "(SELECT id FROM items ORDER BY id) nested",
                FmDataActionExecutor.stripTopLevelOrderBy(sql));
        String withoutOuterOrder = "SELECT \"x ORDER BY y\" AS label FROM items";
        assertEquals(withoutOuterOrder,
                FmDataActionExecutor.stripTopLevelOrderBy(withoutOuterOrder));
        assertEquals("SELECT 'it''s ORDER BY safe' AS label FROM items",
                FmDataActionExecutor.stripTopLevelOrderBy(
                        "SELECT 'it''s ORDER BY safe' AS label FROM items ORDER BY label"));
    }

    @Test
    void paginatesAtDatabaseInsteadOfLoadingAllPrecedingRows() throws Exception {
        String sql = "SELECT ID FROM items ORDER BY ID";
        assertEquals(sql + " LIMIT :__fmPageSize OFFSET :__fmOffset",
                FmDataActionExecutor.pageSql(sql, "MariaDB"));
        assertEquals(sql + " OFFSET :__fmOffset ROWS FETCH NEXT :__fmPageSize ROWS ONLY",
                FmDataActionExecutor.pageSql(sql, "Oracle"));
        assertEquals(sql + " OFFSET :__fmOffset ROWS FETCH NEXT :__fmPageSize ROWS ONLY",
                FmDataActionExecutor.pageSql(sql, "Microsoft SQL Server"));
        assertThrows(org.qifu.base.exception.ServiceException.class,
                () -> FmDataActionExecutor.pageSql("SELECT ID FROM items",
                        "Microsoft SQL Server"));
    }
}
