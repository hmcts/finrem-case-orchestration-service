package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@RunWith(MockitoJUnitRunner.class)
public class AssignApplicantSolicitorServiceTest  {

    private static final long CASE_ID = 1583841721773828L;
    private static final String USER_AUTH = "123456789";
    private static final String TEST_ORG_ID = "1234";
    private static final String TEST_APP_ORG_NAME = "test org";

    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private CcdDataStoreService ccdDataStoreService;
    @Mock
    private PrdOrganisationService prdOrganisationService;

    @InjectMocks
    private AssignApplicantSolicitorService assignApplicantSolicitorService;

    private FinremCaseDetails caseDetails;

    private OrganisationPolicy applicantOrgPolicy;
    private OrganisationsResponse testOrg;

    @Before
    public void setUp() throws Exception {
        FinremCaseData caseData = FinremCaseData.builder().build();
        caseDetails = FinremCaseDetails.builder().id(CASE_ID).data(caseData).build();

        applicantOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_ORG_ID).organisationName(TEST_APP_ORG_NAME).organisationID(TEST_ORG_ID).build())
            .build();
        testOrg = OrganisationsResponse.builder()
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier(TEST_ORG_ID)
            .build();

        caseDetails.getData().setApplicantOrganisationPolicy(applicantOrgPolicy);
        when(prdOrganisationService.retrieveOrganisationsData(USER_AUTH)).thenReturn(testOrg);

    }

    @Test
    public void shouldAssignApplicantSolicitor() {
        assignApplicantSolicitorService.setApplicantSolicitor(caseDetails, USER_AUTH);

        verify(assignCaseAccessService).assignCaseAccess(caseDetails, USER_AUTH);
        verify(ccdDataStoreService).removeCreatorRole(caseDetails, USER_AUTH);
    }

    @Test
    public void shouldThrowExceptionWhenCaseAccessServiceFails() {
        doThrow(feignError()).when(assignCaseAccessService).assignCaseAccess(caseDetails, USER_AUTH);

        assertThrows(AssignCaseAccessException.class, () ->
            assignApplicantSolicitorService.setApplicantSolicitor(caseDetails, USER_AUTH));
    }

    @Test
    public void shouldThrowExceptionWhenGetOrgIdFailsViaNullOrgPolicy() {
        applicantOrgPolicy = null;
        caseDetails.getData().setApplicantOrganisationPolicy(applicantOrgPolicy);

        Exception exception = assertThrows(AssignCaseAccessException.class, () ->
            assignApplicantSolicitorService.setApplicantSolicitor(caseDetails, USER_AUTH));

        String expectedMessage = "Applicant organisation not selected";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void shouldThrowExceptionWhenOrgIdsDoNotMatch() {
        testOrg.setOrganisationIdentifier("1235");

        Exception exception = assertThrows(AssignCaseAccessException.class, () ->
            assignApplicantSolicitorService.setApplicantSolicitor(caseDetails, USER_AUTH));

        String expectedMessage = "Applicant solicitor does not belong to chosen applicant organisation";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

}
