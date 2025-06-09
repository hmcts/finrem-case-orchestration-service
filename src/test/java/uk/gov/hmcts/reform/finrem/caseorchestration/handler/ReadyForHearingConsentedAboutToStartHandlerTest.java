package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIST_FOR_HEARING_COLLECTION_CONSENTED;

class ReadyForHearingConsentedAboutToStartHandlerTest {

    private ReadyForHearingConsentedAboutToStartHandler handler;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(objectMapper);
        ConsentedHearingHelper consentedHearingHelper = new ConsentedHearingHelper(objectMapper);
        handler = new ReadyForHearingConsentedAboutToStartHandler(finremCaseDetailsMapper, consentedHearingHelper);
    }

    @Test
     void canHandle() {
        Assertions.assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.READY_FOR_HEARING);
    }

    @Test
    void givenConsentedCase_WhenHearingNotListed_ThenShouldNotBeReadyForHearing() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getErrors(), is(List.of("There is no hearing on the case.")));
    }

    @Test
    void givenConsentedCase_WhenHearingListingSchedulesOnly_ThenShouldBeReadyForHearing() {

        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();
        ConsentedHearingDataElement consentedHearingDataElement = new ConsentedHearingDataElement();
        consentedHearingDataElement.setHearingDate(LocalDate.now().plusMonths(1).toString());
        List<ConsentedHearingDataWrapper> listForHearings = new ArrayList<>();
        listForHearings.add(new ConsentedHearingDataWrapper(LIST_FOR_HEARING_COLLECTION_CONSENTED, consentedHearingDataElement));
        finremCallbackRequest.getCaseDetails().getData().setListForHearings(listForHearings);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getErrors(), is(empty()));
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.READY_FOR_HEARING)
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .caseType(CaseType.CONSENTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}
