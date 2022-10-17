package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.CaseFlag;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.FlagDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag.FlagDetailData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_APPLICANT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CASE_RESPONDENT_FLAGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class CaseFlagsServiceTest {

    public static final String APPLICANT_NAME = "App Name";
    public static final String RESPONDENT_NAME = "Resp Name";

    @Mock
    private CaseDataService caseDataService;
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private CaseFlagsService caseFlagsService;

    @Test
    public void givenCaseFlags_whenHandleAboutToSubmit_thenSetApplicantFlagDetails() {
        when(caseDataService.buildFullApplicantName(any())).thenReturn(APPLICANT_NAME);

        CaseDetails caseDetails = caseData();
        caseDetails.getData().put(CASE_APPLICANT_FLAGS, flagDetailsData());

        caseFlagsService.setCaseFlagInformation(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();

        CaseFlag applicantFlags = objectMapper.convertValue(caseData.get(CASE_APPLICANT_FLAGS), CaseFlag.class);

        assertThat(applicantFlags.getPartyName(), is(APPLICANT_NAME));
        assertThat(applicantFlags.getRoleOnCase(), is(APPLICANT));
    }

    @Test
    public void givenCaseFlags_whenHandleAboutToSubmit_thenSetRespondentFlagDetails() {
        when(caseDataService.buildFullRespondentName(any())).thenReturn(RESPONDENT_NAME);

        CaseDetails caseDetails = caseData();
        caseDetails.getData().put(CASE_RESPONDENT_FLAGS, flagDetailsData());

        caseFlagsService.setCaseFlagInformation(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();

        CaseFlag respondentFlags = objectMapper.convertValue(caseData.get(CASE_RESPONDENT_FLAGS), CaseFlag.class);

        assertThat(respondentFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentFlags.getRoleOnCase(), is(RESPONDENT));
    }

    @Test
    public void givenNoCaseFlags_whenHandleAboutToSubmit_thenSetDefaultCaseFlagField() {
        when(caseDataService.buildFullApplicantName(any())).thenReturn(APPLICANT_NAME);
        when(caseDataService.buildFullRespondentName(any())).thenReturn(RESPONDENT_NAME);

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
    public void givenNoCaseFlags_whenHandleAboutToSubmit_thenSetCaseLevelFlags() {
        CaseDetails caseDetails = caseData();

        caseFlagsService.setCaseFlagInformation(caseDetails);

        Map<String, Object> caseData = caseDetails.getData();

        CaseFlag caseFlag = objectMapper.convertValue(caseData.get(CASE_LEVEL_FLAGS), CaseFlag.class);
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
}