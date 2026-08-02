package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmProcessVersionView(
        String oid,
        Integer versionNo,
        String versionStatus,
        String bpmnXml,
        String bpmnSha256,
        String flowableDeploymentId,
        String flowableProcessDefId,
        String publishedBy,
        Date publishedDate,
        List<FmTaskFormRuleView> taskForms) {
}
