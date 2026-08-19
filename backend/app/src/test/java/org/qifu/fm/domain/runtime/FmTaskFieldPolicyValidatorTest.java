package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
