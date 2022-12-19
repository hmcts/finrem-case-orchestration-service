package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ListForHearingContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private ListForHearingContestedAboutToStartHandler handler;

    @Before
    public void setup() {
        handler = new ListForHearingContestedAboutToStartHandler(
            new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule())));
    }

    @Test
    public void givenContestedCase_whenEventIsListForHearing_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(true));
    }

    @Test
    public void givenContestedCase_whenEventIsNotListForHearing_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenContestedCase_whenCallbackIsSubmit_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.LIST_FOR_HEARING),
            is(false));
    }

    @Test
    public void givenConsentCase_whenEventIsListForHearing_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.LIST_FOR_HEARING),
            is(false));
    }


    @Test
    public void givenCase_whenEventStart_thenSetDefaultOptionToNo() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(YesOrNo.NO, response.getData().getAdditionalHearingDocumentsOption());
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .<FinremCaseDetails>builder()
            .eventType(EventType.LIST_FOR_HEARING)
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .caseType(uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}