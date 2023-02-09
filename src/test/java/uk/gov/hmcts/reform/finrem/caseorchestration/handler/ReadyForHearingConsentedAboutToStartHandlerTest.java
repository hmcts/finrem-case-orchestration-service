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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIST_FOR_HEARING_COLLECTION_CONSENTED;

@RunWith(MockitoJUnitRunner.class)
public class ReadyForHearingConsentedAboutToStartHandlerTest extends BaseHandlerTest {

    private ReadyForHearingConsentedAboutToStartHandler handler;

    private static final String AUTH_TOKEN = "token:)";

    @Before
    public void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
        handler = new ReadyForHearingConsentedAboutToStartHandler(finremCaseDetailsMapper);
    }

    @Test
    public void canHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.READY_FOR_HEARING),
            is(true));
    }

    @Test
    public void canNotHandleWrongCaseType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.READY_FOR_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEvent() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.READY_FOR_HEARING),
            is(false));
    }


    @Test
    public void givenConsentedCase_WhenHearingNotListed_ThenShouldNotBeReadyForHearing() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertEquals(List.of("There is no hearing on the case."), response.getErrors());
    }


    @Test
    public void givenConsentedCase_WhenHearingListingSchedulesOnly_ThenShouldBeReadyForHearing() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        ConsentedHearingDataElement consentedHearingDataElement = new ConsentedHearingDataElement();
        consentedHearingDataElement.setHearingDate(LocalDate.now().plusMonths(1).toString());
        List<ConsentedHearingDataWrapper> listForHearings = new ArrayList<>();
        listForHearings.add(new ConsentedHearingDataWrapper(LIST_FOR_HEARING_COLLECTION_CONSENTED, consentedHearingDataElement));
        finremCallbackRequest.getCaseDetails().getData().setListForHearings(listForHearings);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertNull(response.getErrors());
    }


    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .<FinremCaseDetails>builder()
            .eventType(EventType.READY_FOR_HEARING)
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .caseType(CaseType.CONSENTED)
                .data(new FinremCaseData()).build())
            .build();
    }


}
