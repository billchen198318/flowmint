package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

import tools.jackson.databind.ObjectMapper;

class FmAssignmentRuleConfigValidatorTest {

    private final FmAssignmentRuleConfigValidator validator =
            new FmAssignmentRuleConfigValidator(new ObjectMapper());

    @Test
    void acceptsResolverSpecificConfiguration() {
        assertDoesNotThrow(() -> validator.validate(
                "FIXED_ACCOUNT", "{\"accounts\":[\"user01\"]}", null));
        assertDoesNotThrow(() -> validator.validate(
                "APPROVAL_GROUP", "{\"approvalGroupId\":\"group01\"}", "{}"));
        assertDoesNotThrow(() -> validator.validate(
                "APPROVAL_AUTHORITY",
                "{\"approvalAuthorityId\":\"authority01\"}", null));
    }

    @Test
    void rejectsMissingRequiredIdentifier() {
        assertThrows(ServiceException.class,
                () -> validator.validate("APPROVAL_GROUP", "{}", null));
        assertThrows(ServiceException.class,
                () -> validator.validate("FIXED_ACCOUNT", "{\"accounts\":[]}", null));
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(ServiceException.class,
                () -> validator.validate("DIRECT_MANAGER", "not-json", null));
        assertThrows(ServiceException.class,
                () -> validator.validate("DIRECT_MANAGER", "{}", "[]"));
    }
}
