package org.qifu.fm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.qifu.fm.dto.command.FmIncidentReassignRequest;
import org.qifu.fm.dto.command.FmIncidentRetryRequest;
import org.qifu.fm.dto.command.FmProcessTerminateRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class FmIncidentOperationsControllerContractTest {

    @Test
    void exposesOnlyTenantScopedIncidentOperations() {
        assertEquals("/api/fm/operations",
                FmIncidentOperationsController.class.getAnnotation(
                        RequestMapping.class).value()[0]);
        Set<String> paths = Arrays.stream(FmIncidentOperationsController.class
                .getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(annotation -> annotation != null)
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .collect(Collectors.toSet());
        assertEquals(Set.of("/incidents", "/incidents/reassign",
                "/incidents/reassign-options", "/incidents/retry",
                "/process-instances", "/process-instances/terminate"), paths);
    }

    @Test
    void reassignBodyCannotOverrideTenantOrActor() {
        Set<String> components = Arrays.stream(FmIncidentReassignRequest.class
                .getRecordComponents()).map(component -> component.getName())
                .collect(Collectors.toSet());
        assertEquals(Set.of("incidentId", "targetAccount", "reason"), components);
        assertFalse(components.contains("tenantId"));
        assertFalse(components.contains("actor"));
    }

    @Test
    void retryAndTerminateBodiesCannotOverrideTenantOrActor() {
        Set<String> retry = Arrays.stream(FmIncidentRetryRequest.class
                .getRecordComponents()).map(component -> component.getName())
                .collect(Collectors.toSet());
        Set<String> terminate = Arrays.stream(FmProcessTerminateRequest.class
                .getRecordComponents()).map(component -> component.getName())
                .collect(Collectors.toSet());
        assertEquals(Set.of("incidentId", "reason"), retry);
        assertEquals(Set.of("processInstanceId", "reason"), terminate);
    }
}
