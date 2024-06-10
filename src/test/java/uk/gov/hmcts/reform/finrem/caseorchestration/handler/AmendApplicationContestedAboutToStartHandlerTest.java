package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;

public class AmendApplicationContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private AmendApplicationContestedAboutToStartHandler handler;

    @Before
    public void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = Mockito.mock(FinremCaseDetailsMapper.class);
        handler = new AmendApplicationContestedAboutToStartHandler(finremCaseDetailsMapper,
            new OnStartDefaultValueService());
    }

    @Test
    public void givenContestedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.ISSUE_APPLICATION),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS),
            is(true));
    }

    @Test
    public void handle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        handler.handle(callbackRequest, AUTH_TOKEN);
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(NO_VALUE, response.getData().getCivilPartnership().getYesOrNo());
        assertEquals(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS,
            response.getData().getScheduleOneWrapper().getTypeOfApplication());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}