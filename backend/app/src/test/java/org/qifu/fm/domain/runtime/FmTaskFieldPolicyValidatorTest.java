package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

import tools.jackson.databind.ObjectMapper;

class FmTaskFieldPolicyValidatorTest {

    private final FmTaskFieldPolicyValidator validator =
            new FmTaskFieldPolicyValidator(new ObjectMapper());

    @Test
    void allowsOnlyEditFieldsToChange() {
        String policy = "{\"default\":\"READ\",\"fields\":{\"comment\":\"EDIT\"}}";
        assertDoesNotThrow(() -> validator.validateChanges(
                policy,
                Map.of("amount", 100, "comment", "old"),
                Map.of("amount", 100, "comment", "new")));
        assertThrows(ServiceException.class, () -> validator.validateChanges(
                policy,
                Map.of("amount", 100, "comment", "old"),
                Map.of("amount", 200, "comment", "new")));
    }

    @Test
    void rejectsInvalidPolicy() {
        assertThrows(ServiceException.class,
                () -> validator.validateConfiguration(
                        "{\"default\":\"ADMIN\",\"fields\":{}}"));
    }

    @Test
    void comparesFormioFileObjectsByAttachmentId() {
        String policy = "{\"default\":\"READ\",\"fields\":{}}";
        String attachmentId = "53d6f9bf-f3b4-446a-9d4d-d088d818b87a";
        assertDoesNotThrow(() -> validator.validateChanges(
                policy,
                Map.of("attachments", List.of(attachmentId)),
                Map.of("attachments", List.of(Map.of(
                        "name", "hata.png",
                        "url", "#flowmint-attachment-" + attachmentId,
                        "size", 217760)))));
        assertThrows(ServiceException.class, () -> validator.validateChanges(
                policy,
                Map.of("attachments", List.of(attachmentId)),
                Map.of("attachments", List.of(Map.of(
                        "attachmentId", "different-attachment")))));
    }
}
