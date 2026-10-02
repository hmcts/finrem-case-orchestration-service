package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.organisationPolicy;

@ExtendWith(MockitoExtension.class)
class PartyServiceTest {

    private static final String APPLICANT_ORG_POLICY_ASSIGNED_ROLE = "APPLICANT_ORG_POLICY_ASSIGNED_ROLE";
    private static final String RESPONDENT_ORG_POLICY_ASSIGNED_ROLE = "RESPONDENT_ORG_POLICY_ASSIGNED_ROLE";

    @InjectMocks
    private PartyService partyService;

    @Mock
    private FinremCaseDetails finremCaseDetails;

    @Mock
    private FinremCaseData finremCaseData;

    @BeforeEach
    void setUp() {
        when(finremCaseDetails.getData()).thenReturn(finremCaseData);
    }

    @Test
    void givenOnlyApplicantRepresented_getAllActivePartyList_shouldBuildSelectedApplicantSolicitor() {
        when(finremCaseData.getApplicantOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, APPLICANT_ORG_POLICY_ASSIGNED_ROLE));
        when(finremCaseData.getFullApplicantName()).thenReturn("Applicant A");
        when(finremCaseData.getRespondentFullName()).thenReturn("Respondent A");

        DynamicMultiSelectList dynamicMultiSelectList = partyService.getAllActivePartyList(finremCaseDetails);

        assertThat(dynamicMultiSelectList.getValue())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(
                tuple(APPLICANT_ORG_POLICY_ASSIGNED_ROLE, "Applicant - Applicant A"),
                tuple(CaseRole.RESP_SOLICITOR.getCcdCode(), "Respondent - Respondent A"));
    }

    @Test
    void givenOnlyRespondentRepresented_getAllActivePartyList_shouldBuildSelectedApplicantSolicitor() {
        when(finremCaseData.getRespondentOrganisationPolicy())
            .thenReturn(organisationPolicy(TEST_ORG_ID, RESPONDENT_ORG_POLICY_ASSIGNED_ROLE));
        when(finremCaseData.getFullApplicantName()).thenReturn("Applicant A");
        when(finremCaseData.getRespondentFullName()).thenReturn("Respondent A");

        DynamicMultiSelectList dynamicMultiSelectList = partyService.getAllActivePartyList(finremCaseDetails);

        assertThat(dynamicMultiSelectList.getValue())
            .extracting(DynamicMultiSelectListElement::getCode, DynamicMultiSelectListElement::getLabel)
            .containsExactly(
                tuple(RESPONDENT_ORG_POLICY_ASSIGNED_ROLE, "Respondent - Respondent A"),
                tuple(CaseRole.APP_SOLICITOR.getCcdCode(), "Applicant - Applicant A"));
    }
}
