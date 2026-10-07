package org.qifu.fm.domain.tenant;

import javax.sql.DataSource;

import org.qifu.base.support.TokenStoreBuilder;
import org.springframework.stereotype.Component;

@Component
public class FmAccountTokenRevoker {

    private final DataSource dataSource;

    public FmAccountTokenRevoker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void revokeAll(String accountId) {
        TokenStoreBuilder.build(dataSource).revokeAllForUser(accountId);
    }
}
