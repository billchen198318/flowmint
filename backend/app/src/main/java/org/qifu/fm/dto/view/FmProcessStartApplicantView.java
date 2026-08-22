package org.qifu.fm.dto.view;

public record FmProcessStartApplicantView(
        String account,
        String displayName,
        String primaryOrgUnitName,
        boolean self) {
}
