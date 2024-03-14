package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToSubmitHandlerTest extends BaseHandlerTestSetup {

    @InjectMocks
    private InterimHearingContestedAboutToSubmitHandler interimHearingContestedAboutToSubmitHandler;
    @Mock
    private InterimHearingService interimHearingService;

    private InterimHearingHelper interimHearingHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-old-two-new-collections.json";

    @Before
    public void setup() {
        interimHearingHelper = new InterimHearingHelper(objectMapper);
    }

    @Test
    public void canHandle() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandleWrongCaseType() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEvent() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void givenContestedCase_WhenMultipleInterimHearing_ThenHearingsShouldBePresentInChronologicalOrder() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(TEST_JSON);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handlerResult =
            interimHearingContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        List<InterimHearingCollection> interimHearingList =
            handlerResult.getData().getInterimWrapper().getInterimHearings();

        assertThat(interimHearingList.get(0).getValue().getInterimHearingDate().toString(), is("2000-10-10"));
        assertThat(interimHearingList.get(1).getValue().getInterimHearingDate().toString(), is("2030-10-10"));
        assertThat(interimHearingList.get(2).getValue().getInterimHearingDate().toString(), is("2040-10-10"));
        assertThat(interimHearingList.get(3).getValue().getInterimHearingDate().toString(), is("2050-10-10"));

        verify(interimHearingService).addHearingNoticesToCase(any(), any());
    }

    @Test
    public void shouldHandleErrorsReturnedFromService() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest(TEST_JSON);
        when(interimHearingService.getValidationErrors(any())).thenReturn(List.of("Error 1", "Error 2"));
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            interimHearingContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors().size(), is(2));
    }
}
