package org.qifu.fm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.dto.command.FmGroovyPreviewCommand;
import org.qifu.fm.dto.view.FmGroovyPreviewView;
import org.qifu.fm.logic.IFmGroovyPreviewLogicService;

class FmGroovyPreviewControllerTest {

    @Test
    void missingFieldsReturnQifuCheckFieldsBeforeLogic() {
        var logic = mock(IFmGroovyPreviewLogicService.class);
        var controller = new FMGroovyPreviewController(logic);
        var response = controller.preview(new FmGroovyPreviewCommand(null, null, null, null, null, null)).getBody();
        assertTrue(response.getCheckFields().containsKey("oid"));
        assertTrue(response.getCheckFields().containsKey("expectedLockVersion"));
        assertTrue(response.getCheckFields().containsKey("groovyBindings"));
        verifyNoInteractions(logic);
    }

    @Test
    void checkAcceptsMissingSampleButPreviewRequiresIt() throws Exception {
        var logic = mock(IFmGroovyPreviewLogicService.class);
        var controller = new FMGroovyPreviewController(logic);
        var binding = new FmGroovyBindingCommand("node", "binding", "return [:]", "{}", "{}", "{}", 3000);
        var command = new FmGroovyPreviewCommand("V1", 1, "xml", List.of(binding), "node", null);
        assertTrue(controller.preview(command).getBody().getCheckFields().containsKey("sampleInput"));
        verifyNoInteractions(logic);
        when(logic.check(any())).thenReturn(new DefaultResult<FmGroovyPreviewView>());
        controller.check(command);
        verify(logic).check(command);
    }

    @Test
    void bothEndpointsRequireExistingUpdateProgramPermission() throws Exception {
        for (String name : List.of("check", "preview")) {
            var permission = FMGroovyPreviewController.class.getMethod(name, FmGroovyPreviewCommand.class)
                    .getAnnotation(ControllerMethodAuthority.class);
            assertTrue(permission.check());
            assertEquals("FM_PROG004D0001U", permission.programId());
        }
    }
}
