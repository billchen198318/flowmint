package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

import org.qifu.fm.dto.command.FmGroovyBindingCommand;

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
        List<FmTaskFormRuleView> taskForms,
        List<FmTaskPolicyView> taskPolicies,
        List<FmTaskAssignmentRuleView> assignmentRules,
        List<FmProcessStartPolicyView> startPolicies,
        List<FmGroovyBindingCommand> groovyBindings,
        Integer lockVersion,
        boolean groovyDraftEnabled) {
}
