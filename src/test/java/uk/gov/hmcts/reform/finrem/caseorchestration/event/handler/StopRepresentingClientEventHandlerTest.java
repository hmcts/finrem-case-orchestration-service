package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.StopRepresentingClientEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;

@ExtendWith(MockitoExtension.class)
class StopRepresentingClientEventHandlerTest {

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    private StopRepresentingClientEventHandler underTest;

    @BeforeEach
    public void setup() {
        underTest = new StopRepresentingClientEventHandler(assignCaseAccessService, systemUserService, finremCaseDetailsMapper);
        when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
    }

    @Test
    void givenOrganisationsToAddOrRemove_whenHandled_thenCallAssignmentApi() {
        FinremCaseData caseData = spy(FinremCaseData.class);
        ChangeOrganisationRequest changeRequest = mock(ChangeOrganisationRequest.class);
        when(changeRequest.isNoOrganisationsToAddOrRemove()).thenReturn(false);
        caseData.getContactDetailsWrapper().setNocParty(NoticeOfChangeParty.APPLICANT);
        when(caseData.getChangeOrganisationRequestField()).thenReturn(changeRequest);
        FinremCaseData caseDataBefore = mock(FinremCaseData.class);
        OrganisationPolicy originalAppOrgPolicy = mock(OrganisationPolicy.class);
        when(caseDataBefore.getApplicantOrganisationPolicy()).thenReturn(originalAppOrgPolicy);

        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(caseData).build();

        StopRepresentingClientEvent event = StopRepresentingClientEvent.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
            .userAuthorisation(AUTH_TOKEN)
            .build();

        CaseDetails mockValidCaseDetails = mock(CaseDetails.class);
        CaseDetails mockInvalidCaseDetails = mock(CaseDetails.class);
        lenient().when(finremCaseDetailsMapper.mapToCaseDetails(any(FinremCaseDetails.class))).thenReturn(mockInvalidCaseDetails);
        when(finremCaseDetailsMapper.mapToCaseDetails(argThat(cd
            -> cd.getData().getApplicantOrganisationPolicy().equals(originalAppOrgPolicy)
        ))).thenReturn(mockValidCaseDetails);

        underTest.handleEvent(event);

        verify(assignCaseAccessService).findAndRevokeCreatorRole(CASE_ID);
        verify(assignCaseAccessService).applyDecision(TEST_SYSTEM_TOKEN, mockValidCaseDetails);
        verify(assignCaseAccessService, never()).applyDecision(TEST_SYSTEM_TOKEN, mockInvalidCaseDetails);
    }

    // TODO test RESPONDENT
    // TODO test hasInvalidOrgPolicy
}
