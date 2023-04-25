package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.CaseFlag;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.FlagDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.FlagDetailData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseFlagsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

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

    private static final String APPLICANT_NAME = "App Name";
    private static final String RESPONDENT_NAME = "Resp Name";
    private static final String CASE = "Case";
    @Mock
    private CaseDataService caseDataService;
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private CaseFlagsService caseFlagsService;

    @Test
    public void shouldSetApplicantRespondentAndCaseFlagDetails() {

        FinremCaseDetails caseDetails = finremCaseDetails();

        caseFlagsService.setCaseFlagInformation(caseDetails);

        CaseFlagsWrapper caseFlagsWrapper = caseDetails.getData().getCaseFlagsWrapper();
        assertThat(caseFlagsWrapper.getApplicantFlags().getPartyName(), is(APPLICANT_NAME));
        assertThat(caseFlagsWrapper.getApplicantFlags().getRoleOnCase(), is(APPLICANT));
        assertThat(caseFlagsWrapper.getRespondentFlags().getPartyName(), is(RESPONDENT_NAME));
        assertThat(caseFlagsWrapper.getRespondentFlags().getRoleOnCase(), is(RESPONDENT));
        assertThat(caseFlagsWrapper.getCaseFlags().getPartyName(), is(CASE));
        assertThat(caseFlagsWrapper.getCaseFlags().getRoleOnCase(), is(CASE));
    }

    private FinremCaseDetails finremCaseDetails() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .ccdCaseType(CaseType.CONTESTED)
                .contactDetailsWrapper(ContactDetailsWrapper.builder()
                    .applicantFmName("App")
                    .applicantLname("Name")
                    .respondentFmName("Resp")
                    .respondentLname("Name")
                    .build())
                .build())
            .build();
        return caseDetails;
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