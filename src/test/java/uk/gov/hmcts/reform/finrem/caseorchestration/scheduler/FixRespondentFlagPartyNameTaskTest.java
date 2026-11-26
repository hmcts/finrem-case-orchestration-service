package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.caseflag.caseflag.CaseFlag;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseFlagsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FixRespondentFlagPartyNameTaskTest {

    private static final String APPLICANT_NAME = "Bilbo Baggins";
    private static final String RESPONDENT_NAME = "Smeagol Gollum";

    @Mock
    private CaseReferenceCsvLoader csvLoader;
    @Mock
    private CcdService ccdService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private FixRespondentFlagPartyNameTask task;

    @BeforeEach
    void setUp() {
        task = new FixRespondentFlagPartyNameTask(csvLoader, ccdService, systemUserService, finremCaseDetailsMapper);
    }

    @Test
    void givenRespondentFlagHasApplicantName_whenExecuteTask_thenPartyNameCorrected() {
        FinremCaseDetails details = caseDetails(APPLICANT_NAME);

        task.executeTask(details);

        CaseFlagsWrapper flags = details.getData().getCaseFlagsWrapper();
        assertThat(flags.getRespondentFlags().getPartyName()).isEqualTo(RESPONDENT_NAME);
        assertThat(flags.getApplicantFlags().getPartyName()).isEqualTo(APPLICANT_NAME);
    }

    @Test
    void givenRespondentFlagAlreadyCorrect_whenExecuteTask_thenUnchanged() {
        FinremCaseDetails details = caseDetails(RESPONDENT_NAME);

        task.executeTask(details);

        assertThat(details.getData().getCaseFlagsWrapper().getRespondentFlags().getPartyName())
            .isEqualTo(RESPONDENT_NAME);
    }

    private FinremCaseDetails caseDetails(String respondentFlagPartyName) {
        FinremCaseData data = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantFmName("Bilbo")
                .applicantLname("Baggins")
                .respondentFmName("Smeagol")
                .respondentLname("Gollum")
                .build())
            .caseFlagsWrapper(CaseFlagsWrapper.builder()
                .applicantFlags(CaseFlag.builder()
                    .partyName(APPLICANT_NAME).details(new ArrayList<>()).build())
                .respondentFlags(CaseFlag.builder()
                    .partyName(respondentFlagPartyName).details(new ArrayList<>()).build())
                .build())
            .build();

        return FinremCaseDetails.builder()
            .id(123456789L)
            .caseType(CaseType.CONTESTED)
            .data(data)
            .build();
    }
}
