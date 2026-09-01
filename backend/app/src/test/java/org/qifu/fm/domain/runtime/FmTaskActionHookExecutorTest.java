package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.logic.IFmDataActionLogicService;

import tools.jackson.databind.ObjectMapper;

class FmTaskActionHookExecutorTest {

    private final IFmDataActionLogicService actionLogic =
            mock(IFmDataActionLogicService.class);
    private final FmTaskActionHookExecutor executor =
            new FmTaskActionHookExecutor(actionLogic, new ObjectMapper());

    @Test
    void executesMatchingHookAndMapsControlledResponse() throws Exception {
        DefaultResult<FmDataActionExecutionView> result = new DefaultResult<>();
        result.setValue(new FmDataActionExecutionView(
                "execution", "GA_VALIDATE", 3, false,
                Map.of("validation", Map.of(
                        "valid", 1,
                        "requiresTechnicalAcceptance", 1))));
        when(actionLogic.execute(eq("A01"), eq("GA_VALIDATE"), eq(3),
                any(), eq("tester"))).thenReturn(result);
        Map<String, Object> form = new HashMap<>();
        form.put("purchaseOrderBusinessKey", "PO-1");
        form.put("acceptanceItems", java.util.List.of(Map.of(
                "orderLineNo", 1, "acceptedQuantity", 2)));
        String uiSchema = "{\"taskActionHooks\":[{"
                + "\"hookId\":\"validate\","
                + "\"phase\":\"BEFORE_FORM_UPDATE\","
                + "\"taskDefKey\":\"requesterAcceptance\","
                + "\"actionTypes\":[\"APPROVE\"],"
                + "\"actionCode\":\"GA_VALIDATE\","
                + "\"actionVersion\":3,"
                + "\"requestMapping\":{"
                + "\"purchaseOrderBusinessKey\":\"form.purchaseOrderBusinessKey\","
                + "\"acceptanceItemsJson\":\"json:form.acceptanceItems\"},"
                + "\"successPath\":\"validation.valid\","
                + "\"messagePath\":\"validation.message\","
                + "\"responseMapping\":{"
                + "\"validation.requiresTechnicalAcceptance\":"
                + "\"form.requiresTechnicalAcceptance\"}}]}";

        executor.execute(uiSchema, FmTaskActionHookExecutor.BEFORE_FORM_UPDATE,
                "A01", "requesterAcceptance", "APPROVE", "tester", form, Map.of());

        assertEquals(1, form.get("requiresTechnicalAcceptance"));
    }

    @Test
    void rejectsFailedControlledValidation() throws Exception {
        DefaultResult<FmDataActionExecutionView> result = new DefaultResult<>();
        result.setValue(new FmDataActionExecutionView(
                "execution", "GA_VALIDATE", 3, false,
                Map.of("validation", Map.of("valid", 0, "message", "驗收數量超額"))));
        when(actionLogic.execute(eq("A01"), eq("GA_VALIDATE"), eq(3),
                any(), eq("tester"))).thenReturn(result);
        String uiSchema = "{\"taskActionHooks\":[{"
                + "\"hookId\":\"validate\","
                + "\"phase\":\"BEFORE_FORM_UPDATE\","
                + "\"taskDefKey\":\"*\","
                + "\"actionTypes\":[\"APPROVE\"],"
                + "\"actionCode\":\"GA_VALIDATE\","
                + "\"actionVersion\":3,"
                + "\"requestMapping\":{},"
                + "\"successPath\":\"validation.valid\","
                + "\"messagePath\":\"validation.message\"}]}";

        assertThrows(ServiceException.class, () -> executor.execute(
                uiSchema, FmTaskActionHookExecutor.BEFORE_FORM_UPDATE,
                "A01", "requesterAcceptance", "APPROVE", "tester",
                new HashMap<>(), Map.of()));
    }
}
