package org.qifu.fm.domain.form;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

class FmFormScriptContractValidatorTest {

    private final FmFormScriptContractValidator validator =
            new FmFormScriptContractValidator();

    @Test
    void acceptsBlankAndSupportedLifecycleModule() {
        assertDoesNotThrow(() -> validator.validate(""));
        assertDoesNotThrow(() -> validator.validate("""
                return {
                  async onFormLoad(ctx) { ctx.log("loaded"); },
                  beforeSubmit(ctx) { return true; },
                  onDataActionSuccess: async (ctx) => ctx.redraw()
                };
                """));
    }

    @Test
    void rejectsMissingReturnObjectAndModuleSyntax() {
        assertThrows(ServiceException.class,
                () -> validator.validate("const hooks = {};"));
        assertThrows(ServiceException.class,
                () -> validator.validate("export default {};"));
    }

    @Test
    void rejectsUnsupportedLifecycleName() {
        assertThrows(ServiceException.class, () -> validator.validate("""
                return { async onFormSave(ctx) {} };
                """));
    }
}
