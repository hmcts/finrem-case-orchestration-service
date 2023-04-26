package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseFlagsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class CreateCaseFlagAboutToStartHandlerTest {

    private static final String AUTH_TOKEN = "4d73f8d4-2a8d-48e2-af91-11cbaa642345";
    private static final String APPLICANT_NAME = "App Name";
    private static final String RESPONDENT_NAME = "Resp Name";
    private static final String CASE = "Case";
    private CreateCaseFlagAboutToStartHandler handler;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @BeforeEach
    void setUp() {
        handler = new CreateCaseFlagAboutToStartHandler(finremCaseDetailsMapper, new CaseFlagsService());
    }

    @Test
    void givenCase_whenEventIsCreateCaseFlagConsentedCase_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CREATE_CASE_FLAG),
            is(true));
    }

    @Test
    void givenCase_whenEventIsCreateCaseFlagContestedCase_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CREATE_CASE_FLAG),
            is(true));
    }

    @Test
    void givenCase_whenWrongEventTypeAndContestedCase_thenCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_APP_DETAILS),
            is(false));
    }

    @Test
    void givenCase_whenWrongEventTypeAndConsentedCase_thenCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    void givenCase_whenWrongCallbackType_thenCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CREATE_CASE_FLAG),
            is(false));
    }

    @Test
    void givenCase_whenCreateCaseFlagEvent_thenShouldCreateCaseFlags() {
        FinremCallbackRequest callbackRequest = callbackRequest();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);

        final FinremCaseData responseData = response.getData();
        final CaseFlagsWrapper caseFlagsWrapper = responseData.getCaseFlagsWrapper();

        assertThat(caseFlagsWrapper.getApplicantFlags().getPartyName(), is(APPLICANT_NAME));
        assertThat(caseFlagsWrapper.getApplicantFlags().getRoleOnCase(), is(APPLICANT));
        assertThat(caseFlagsWrapper.getRespondentFlags().getPartyName(), is(RESPONDENT_NAME));
        assertThat(caseFlagsWrapper.getRespondentFlags().getRoleOnCase(), is(RESPONDENT));
        assertThat(caseFlagsWrapper.getCaseFlags().getPartyName(), is(CASE));
        assertThat(caseFlagsWrapper.getCaseFlags().getRoleOnCase(), is(CASE));
    }

    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
            .<FinremCaseDetails>builder()
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .data(FinremCaseData.builder()
                    .ccdCaseType(CaseType.CONTESTED)
                    .contactDetailsWrapper(ContactDetailsWrapper.builder()
                        .applicantFmName("App")
                        .applicantLname("Name")
                        .respondentFmName("Resp")
                        .respondentLname("Name")
                        .build())
                    .build()).build())
            .build();
    }
}