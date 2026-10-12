package org.qifu.fm.domain.datasource;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;

public final class FmDataSourceValidationQuery {

    private static final Pattern SAFE_PROBE = Pattern.compile(
            "(?i)^SELECT\\s+1(?:\\s+FROM\\s+DUAL)?\\s*;?$");

    private FmDataSourceValidationQuery() {
    }

    public static void requireSafe(String query) throws ServiceException {
        if (StringUtils.isBlank(query)) return;
        if (!SAFE_PROBE.matcher(query.trim()).matches()) {
            throw new ServiceException(
                    "連線驗證 SQL 只允許 SELECT 1 或 SELECT 1 FROM DUAL");
        }
    }
}
