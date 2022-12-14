package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedChildrenDetailDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedChildrenService;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHILDREN_COLLECTION;

public class AmendApplicationContestedMidHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private AmendApplicationContestedMidHandler handler;
    private ContestedChildrenService service;
    private ObjectMapper objectMapper;
    private static final String TEST_JSON = "/fixtures/contested/schedule-1-children.json";

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        service = new ContestedChildrenService(objectMapper);
        handler =  new AmendApplicationContestedMidHandler(service);
    }

    @Test
    public void givenContestedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS),
            is(true));
    }

    @Test
    public void givenContestedCase_whenChildLivesOutsideEnglandOrWales_thenHandlerRejectsAndReturnError() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertTrue(response.getErrors().get(0)
            .contains("The court does not have jurisdiction as the child is not habitually resident in England or Wales"));
    }

    @Test
    public void givenContestedCase_whenChildLivesInEnglandOrWales_thenHandlerCanHandle() {
        CallbackRequest callbackRequest = buildCallbackRequest();
        List<ContestedChildrenDetailDataWrapper> children = service.getChildren(callbackRequest.getCaseDetails().getData());
        children.forEach(child -> child.getValue().setChildrenLivesInEnglandOrWales(YES_VALUE));
        callbackRequest.getCaseDetails().getData().put(CHILDREN_COLLECTION, children);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertEquals(0, response.getErrors().size());
    }

    private CallbackRequest buildCallbackRequest()  {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(TEST_JSON)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}