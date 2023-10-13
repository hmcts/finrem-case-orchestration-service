package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.CaseFlag;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.FlagDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.FlagDetailData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_APPLICANT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_RESPONDENT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;

@ExtendWith(MockitoExtension.class)
class CaseFlagsServiceTest {

    static final String APPLICANT_NAME = "App Name";
    static final String RESPONDENT_NAME = "Resp Name";

    @Mock
    private CaseDataService caseDataService;
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private CaseFlagsService caseFlagsService;

    @Test
    void givenCaseFlags_whenHandleAboutToSubmit_thenSetApplicantFlagDetails() {
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn(APPLICANT_NAME);

        CaseDetails caseDetails = caseData();
        caseDetails.getData().put(CASE_APPLICANT_FLAGS, flagDetailsData());

        caseFlagsService.setCaseFlagInformation(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();

        CaseFlag applicantFlags = objectMapper.convertValue(caseData.get(CASE_APPLICANT_FLAGS), CaseFlag.class);

        assertThat(applicantFlags.getPartyName(), is(APPLICANT_NAME));
        assertThat(applicantFlags.getRoleOnCase(), is(APPLICANT));
    }

    @Test
    void givenCaseFlags_whenHandleAboutToSubmit_thenSetApplicantFlagDetails_v1() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();
        finremCaseData.getCaseFlagsWrapper().setApplicantFlags(flagDetailsData());
        finremCaseData.getContactDetailsWrapper().setApplicantFmName("App");
        finremCaseData.getContactDetailsWrapper().setApplicantLname("Name");

        caseFlagsService.setCaseFlagInformation(caseDetails);

        FinremCaseData data = caseDetails.getData();

        CaseFlag applicantFlags = data.getCaseFlagsWrapper().getApplicantFlags();

        assertEquals(APPLICANT_NAME, applicantFlags.getPartyName());
        assertEquals(APPLICANT, applicantFlags.getRoleOnCase());
    }


    @Test
    void givenCaseFlags_whenHandleAboutToSubmit_thenSetRespondentFlagDetails() {
        when(caseDataService.buildFullRespondentName((CaseDetails) any())).thenReturn(RESPONDENT_NAME);

        CaseDetails caseDetails = caseData();
        caseDetails.getData().put(CASE_RESPONDENT_FLAGS, flagDetailsData());

        caseFlagsService.setCaseFlagInformation(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();

        CaseFlag respondentFlags = objectMapper.convertValue(caseData.get(CASE_RESPONDENT_FLAGS), CaseFlag.class);

        assertThat(respondentFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentFlags.getRoleOnCase(), is(RESPONDENT));
    }

    @Test
    void givenCaseFlags_whenHandleAboutToSubmit_thenSetRespondentFlagDetails_v1() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = caseDetails.getData();
        finremCaseData.getCaseFlagsWrapper().setRespondentFlags(flagDetailsData());
        finremCaseData.getContactDetailsWrapper().setAppRespondentFmName("Resp");
        finremCaseData.getContactDetailsWrapper().setAppRespondentLName("Name");

        caseFlagsService.setCaseFlagInformation(caseDetails);

        FinremCaseData data = caseDetails.getData();

        CaseFlag applicantFlags = data.getCaseFlagsWrapper().getRespondentFlags();

        assertEquals(RESPONDENT_NAME, applicantFlags.getPartyName());
        assertEquals(RESPONDENT, applicantFlags.getRoleOnCase());
    }

    @Test
    void givenNoCaseFlags_whenHandleAboutToSubmit_thenSetDefaultCaseFlagField() {
        when(caseDataService.buildFullApplicantName((CaseDetails) any())).thenReturn(APPLICANT_NAME);
        when(caseDataService.buildFullRespondentName((CaseDetails) any())).thenReturn(RESPONDENT_NAME);

        CaseDetails caseDetails = caseData();

        caseFlagsService.setCaseFlagInformation(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();

        CaseFlag respondentFlags = objectMapper.convertValue(caseData.get(CASE_RESPONDENT_FLAGS), CaseFlag.class);
        assertThat(respondentFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentFlags.getRoleOnCase(), is(RESPONDENT));

        CaseFlag applicantFlags = objectMapper.convertValue(caseData.get(CASE_APPLICANT_FLAGS), CaseFlag.class);
        assertThat(applicantFlags.getPartyName(), is(APPLICANT_NAME));
        assertThat(applicantFlags.getRoleOnCase(), is(APPLICANT));
    }

    @Test
    void givenNoCaseFlags_whenHandleAboutToSubmit_thenSetCaseLevelFlags() {
        CaseDetails caseDetails = caseData();

        caseFlagsService.setCaseFlagInformation(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();

        CaseFlag caseFlag = objectMapper.convertValue(caseData.get(CASE_LEVEL_FLAGS), CaseFlag.class);
        assertThat(caseFlag.getPartyName(), is("Case"));
        assertThat(caseFlag.getRoleOnCase(), is("Case"));
    }

    @Test
    void givenNoCaseFlags_whenHandleAboutToSubmit_thenSetCaseLevelFlags_v1() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        caseFlagsService.setCaseFlagInformation(caseDetails);

        FinremCaseData data = caseDetails.getData();

        CaseFlag caseFlag = data.getCaseFlagsWrapper().getCaseFlags();
        assertThat(caseFlag.getPartyName(), is("Case"));
        assertThat(caseFlag.getRoleOnCase(), is("Case"));
    }

    private CaseDetails caseData() {
        Map<String, Object> caseData = new HashMap<>();
        return CaseDetails.builder().data(caseData).build();
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

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.AMEND_CONSENT_ORDER)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONSENTED)
                .data(FinremCaseData.builder().ccdCaseType(CONSENTED).build()).build())
            .build();
    }
}