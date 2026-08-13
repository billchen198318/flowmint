package org.qifu.fm.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

class FmAttachmentUploadControllerContractTest {

    @Test
    void exposesOnlyUploadSessionEndpoints() {
        RequestMapping root = FmAttachmentUploadController.class
                .getAnnotation(RequestMapping.class);
        assertNotNull(root);
        assertArrayEquals(new String[] { "/api/fm/attachments" }, root.value());
        Set<String> paths = Arrays.stream(FmAttachmentUploadController.class
                .getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(annotation -> annotation != null)
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "/sessions", "/sessions/files", "/sessions/files/list",
                "/sessions/files/delete", "/{attachmentId}/delete"), paths);
    }

    @Test
    void everyEndpointRequiresTenantHeader() {
        for (Method method : FmAttachmentUploadController.class.getDeclaredMethods()) {
            if (method.getAnnotation(PostMapping.class) == null) continue;
            Set<String> headers = Arrays.stream(method.getParameterAnnotations())
                    .flatMap(Arrays::stream)
                    .filter(annotation -> annotation instanceof RequestHeader)
                    .map(annotation -> ((RequestHeader) annotation).value())
                    .collect(Collectors.toSet());
            assertEquals(Set.of("X-FlowMint-Tenant"), headers);
        }
    }

    @Test
    void requestBodiesCannotOverrideTenantOrOwner() {
        Set<String> sessionFields = Arrays.stream(
                org.qifu.fm.dto.command.FmAttachmentSessionRequest.class
                        .getRecordComponents())
                .map(component -> component.getName()).collect(Collectors.toSet());
        Set<String> fileFields = Arrays.stream(
                org.qifu.fm.dto.command.FmAttachmentSessionFileRequest.class
                        .getRecordComponents())
                .map(component -> component.getName()).collect(Collectors.toSet());
        assertFalse(sessionFields.contains("tenantId"));
        assertFalse(sessionFields.contains("ownerAccount"));
        assertFalse(fileFields.contains("tenantId"));
        assertFalse(fileFields.contains("ownerAccount"));
    }

    @Test
    void downloadRequiresTenantHeader() throws Exception {
        Method method = FmAttachmentUploadController.class
                .getDeclaredMethod("download", String.class, String.class);
        assertNotNull(method.getAnnotation(GetMapping.class));
        Set<String> headers = Arrays.stream(method.getParameterAnnotations())
                .flatMap(Arrays::stream)
                .filter(annotation -> annotation instanceof RequestHeader)
                .map(annotation -> ((RequestHeader) annotation).value())
                .collect(Collectors.toSet());
        assertEquals(Set.of("X-FlowMint-Tenant"), headers);
    }
}
