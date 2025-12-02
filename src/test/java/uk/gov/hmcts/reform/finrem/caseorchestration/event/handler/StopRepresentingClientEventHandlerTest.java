package uk.gov.hmcts.reform.finrem.caseorchestration.event.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.ArgumentMatchers.eq;
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
        lenient().when(systemUserService.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenOrganisationsToAddOrRemove_whenHandled_thenCallAssignmentApi(boolean isApplicant) {
        FinremCaseData caseData = spy(FinremCaseData.class);
        caseData.getContactDetailsWrapper().setNocParty(isApplicant ? NoticeOfChangeParty.APPLICANT : NoticeOfChangeParty.RESPONDENT);

        ChangeOrganisationRequest changeRequest = mock(ChangeOrganisationRequest.class);
        when(changeRequest.isNoOrganisationsToAddOrRemove()).thenReturn(false);

        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);
        when(caseData.getChangeOrganisationRequestField()).thenReturn(changeRequest);

        // Setting original org policy
        FinremCaseData caseDataBefore = mock(FinremCaseData.class);
        OrganisationPolicy originalOrgPolicy = mock(OrganisationPolicy.class);
        if (isApplicant) {
            when(caseDataBefore.getApplicantOrganisationPolicy()).thenReturn(originalOrgPolicy);
        } else {
            when(caseDataBefore.getRespondentOrganisationPolicy()).thenReturn(originalOrgPolicy);
        }

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(caseData).build();

        StopRepresentingClientEvent event = StopRepresentingClientEvent.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
            .userAuthorisation(AUTH_TOKEN)
            .build();

        // Setting up invalid case details
        CaseDetails mockInvalidCaseDetails = mock(CaseDetails.class);
        lenient().when(finremCaseDetailsMapper.mapToCaseDetails(any(FinremCaseDetails.class)))
            .thenReturn(mockInvalidCaseDetails);

        // Setting up valid case details
        CaseDetails mockValidCaseDetails = mock(CaseDetails.class);
        when(finremCaseDetailsMapper.mapToCaseDetails(argThat(cd
            -> getOrganisationPolicy(cd.getData(), isApplicant).equals(originalOrgPolicy)
            // verifying original appl/resp org policy should be set to finremCaseData
        ))).thenReturn(mockValidCaseDetails);

        underTest.handleEvent(event);

        verify(assignCaseAccessService).findAndRevokeCreatorRole(CASE_ID);
        verify(assignCaseAccessService).applyDecision(TEST_SYSTEM_TOKEN, mockValidCaseDetails);
        verify(assignCaseAccessService, never()).applyDecision(TEST_SYSTEM_TOKEN, mockInvalidCaseDetails);
    }

    @Test
    void givenOrganisationsToAddOrRemove_whenHandled_thenDoesNotCallAssignmentApi() {
        FinremCaseData caseData = spy(FinremCaseData.class);
        ChangeOrganisationRequest changeRequest = mock(ChangeOrganisationRequest.class);
        when(changeRequest.isNoOrganisationsToAddOrRemove()).thenReturn(true);
        caseData.getContactDetailsWrapper().setNocParty(NoticeOfChangeParty.APPLICANT);
        when(caseData.getChangeOrganisationRequestField()).thenReturn(changeRequest);
        FinremCaseData caseDataBefore = mock(FinremCaseData.class);

        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(caseData).build();

        StopRepresentingClientEvent event = StopRepresentingClientEvent.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
            .userAuthorisation(AUTH_TOKEN)
            .build();

        underTest.handleEvent(event);

        verify(assignCaseAccessService).findAndRevokeCreatorRole(CASE_ID);
        verify(assignCaseAccessService, never()).applyDecision(eq(TEST_SYSTEM_TOKEN), any(CaseDetails.class));
    }

    @Test
    void givenNullChangeOrganisationRequest_whenHandled_thenDoesNotCallAssignmentApi() {
        FinremCaseData caseData = spy(FinremCaseData.class);
        caseData.getContactDetailsWrapper().setNocParty(NoticeOfChangeParty.APPLICANT);
        when(caseData.getChangeOrganisationRequestField()).thenReturn(null);
        FinremCaseData caseDataBefore = mock(FinremCaseData.class);

        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(caseData).build();

        StopRepresentingClientEvent event = StopRepresentingClientEvent.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(FinremCaseDetails.builder().data(caseDataBefore).build())
            .userAuthorisation(AUTH_TOKEN)
            .build();

        underTest.handleEvent(event);

        verify(assignCaseAccessService).findAndRevokeCreatorRole(CASE_ID);
        verify(assignCaseAccessService, never()).applyDecision(eq(TEST_SYSTEM_TOKEN), any(CaseDetails.class));
    }

    private OrganisationPolicy getOrganisationPolicy(FinremCaseData caseData, boolean isApplicant) {
        return isApplicant ? caseData.getApplicantOrganisationPolicy() :
            caseData.getRespondentOrganisationPolicy();
    }
}
