package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.CaseFlag;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.FlagDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.FlagDetailData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_APPLICANT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_ROLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_RESPONDENT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class CaseFlagAboutToSubmitHandlerTest {

    public static final String APPLICANT_NAME = "App Name";
    public static final String RESPONDENT_NAME = "Resp Name";

    @Mock
    private CaseDataService caseDataService;
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private CaseFlagAboutToSubmitHandler handler;

    @Test
    public void given_case_when_EventCreateCaseFlag_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CASE_FLAG_CREATE),
            is(true));
    }

    @Test
    public void given_case_when_wrong_callback_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.CASE_FLAG_CREATE),
            is(false));
    }

    @Test
    public void given_case_when_EventCManageCaseFlag_thenCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CASE_FLAG_MANAGE),
            is(true));
    }

    @Test
    public void given_case_when_wrong_eventtype_then_case_can_not_handle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void givenCaseFlags_whenHandleAboutToSubmit_thenSetApplicantFlagDetails() {
        when(caseDataService.buildFullApplicantName(any())).thenReturn(APPLICANT_NAME);

        CallbackRequest request = callbackRequest();
        request.getCaseDetails().getData().put(CASE_APPLICANT_FLAGS, flagDetailsData());

        AboutToStartOrSubmitCallbackResponse response = handler.handle(request, AUTH_TOKEN);

        CaseFlag applicantFlags = objectMapper.convertValue(response.getData().get(CASE_APPLICANT_FLAGS), CaseFlag.class);

        assertThat(applicantFlags.getPartyName(), is(APPLICANT_NAME));
        assertThat(applicantFlags.getRoleOnCase(), is(APPLICANT));
    }

    @Test
    public void givenCaseFlags_whenHandleAboutToSubmit_thenSetRespondentFlagDetails() {
        when(caseDataService.buildFullRespondentName(any())).thenReturn(RESPONDENT_NAME);

        CallbackRequest request = callbackRequest();
        request.getCaseDetails().getData().put(CASE_RESPONDENT_FLAGS, flagDetailsData());

        AboutToStartOrSubmitCallbackResponse response = handler.handle(request, AUTH_TOKEN);

        CaseFlag respondentFlags = objectMapper.convertValue(response.getData().get(CASE_RESPONDENT_FLAGS), CaseFlag.class);

        assertThat(respondentFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentFlags.getRoleOnCase(), is(RESPONDENT));
    }

    @Test
    public void givenCaseFlags_whenHandleAboutToSubmit_thenSetCaseLevelFlagDetails() {
        CallbackRequest request = callbackRequest();
        request.getCaseDetails().getData().put(CASE_LEVEL_FLAGS, flagDetailsData());

        AboutToStartOrSubmitCallbackResponse response = handler.handle(request, AUTH_TOKEN);

        CaseFlag caseLevelFlag = objectMapper.convertValue(response.getData().get(CASE_LEVEL_FLAGS), CaseFlag.class);

        assertThat(caseLevelFlag.getPartyName(), is(CASE_LEVEL_ROLE));
        assertThat(caseLevelFlag.getRoleOnCase(), is(CASE_LEVEL_ROLE));
    }

    @Test
    public void givenNoCaseFlags_whenHandleAboutToSubmit_thenSetDefaultCaseFlagField() {
        when(caseDataService.buildFullApplicantName(any())).thenReturn(APPLICANT_NAME);
        when(caseDataService.buildFullRespondentName(any())).thenReturn(RESPONDENT_NAME);

        CallbackRequest request = callbackRequest();

        AboutToStartOrSubmitCallbackResponse response = handler.handle(request, AUTH_TOKEN);

        CaseFlag caseLevelFlag = objectMapper.convertValue(response.getData().get(CASE_LEVEL_FLAGS), CaseFlag.class);
        assertThat(caseLevelFlag.getPartyName(), is(CASE_LEVEL_ROLE));
        assertThat(caseLevelFlag.getRoleOnCase(), is(CASE_LEVEL_ROLE));

        CaseFlag respondentFlags = objectMapper.convertValue(response.getData().get(CASE_RESPONDENT_FLAGS), CaseFlag.class);
        assertThat(respondentFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentFlags.getRoleOnCase(), is(RESPONDENT));

        CaseFlag applicantFlags = objectMapper.convertValue(response.getData().get(CASE_APPLICANT_FLAGS), CaseFlag.class);
        assertThat(applicantFlags.getPartyName(), is(APPLICANT_NAME));
        assertThat(applicantFlags.getRoleOnCase(), is(APPLICANT));
    }

    private CallbackRequest callbackRequest() {
        Map<String, Object> caseData = new HashMap<>();
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(caseData).build())
            .build();
    }

    private CaseFlag flagDetailsData() {
        return CaseFlag.builder()
            .details(List.of(
                FlagDetailData.builder()
                    .id(UUID.randomUUID().toString())
                    .value(FlagDetail.builder().build())
                    .build()
            )).build();
    }
}