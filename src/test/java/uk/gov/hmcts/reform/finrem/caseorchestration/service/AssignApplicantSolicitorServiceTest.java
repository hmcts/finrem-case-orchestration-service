package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.aspectj.weaver.ast.Call;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.AssignCaseAccessException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;

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
    private FeatureToggleService featureToggleService;
    @Mock
    private PrdOrganisationService prdOrganisationService;

    @InjectMocks
    private AssignApplicantSolicitorService assignApplicantSolicitorService;

    private CaseDetails caseDetails;
    private CallbackRequest callbackRequest;

    private OrganisationPolicy applicantOrgPolicy;
    private OrganisationsResponse testOrg;

    @Before
    public void setUp() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().id(CASE_ID).data(caseData).build();

        applicantOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_ORG_ID).organisationName(TEST_APP_ORG_NAME).organisationID(TEST_ORG_ID).build())
            .build();
        testOrg = OrganisationsResponse.builder()
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier(TEST_ORG_ID)
            .build();

        caseDetails.getData().put(ORGANISATION_POLICY_APPLICANT, applicantOrgPolicy);
        when(featureToggleService.isAssignCaseAccessEnabled()).thenReturn(true);
        when(prdOrganisationService.retrieveOrganisationsData(USER_AUTH)).thenReturn(testOrg);

    }

    @Test
    public void shouldAssignApplicantSolicitor() {
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        assignApplicantSolicitorService.setApplicantSolicitor(callbackRequest, USER_AUTH);

        verify(assignCaseAccessService).assignCaseAccess(caseDetails, USER_AUTH);
        verify(ccdDataStoreService).removeCreatorRole(caseDetails, USER_AUTH);
    }

    @Test
    public void shouldThrowExceptionWhenCaseAccessServiceFails() {
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        doThrow(feignError()).when(assignCaseAccessService).assignCaseAccess(caseDetails, USER_AUTH);

        assertThrows(AssignCaseAccessException.class, () ->
        assignApplicantSolicitorService.setApplicantSolicitor(callbackRequest, USER_AUTH));
    }

    @Test
    public void shouldThrowExceptionWhenGetOrgIdFailsViaNullOrgPolicy() {
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        applicantOrgPolicy = null;
        caseDetails.getData().put(ORGANISATION_POLICY_APPLICANT, applicantOrgPolicy);

        Exception exception = assertThrows(AssignCaseAccessException.class, () ->
            assignApplicantSolicitorService.setApplicantSolicitor(callbackRequest, USER_AUTH));

        String expectedMessage = "Applicant organisation not selected";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void shouldThrowExceptionWhenOrgIdsDoNotMatch(){
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        testOrg.setOrganisationIdentifier("1235");

        Exception exception = assertThrows(AssignCaseAccessException.class, () ->
            assignApplicantSolicitorService.setApplicantSolicitor(callbackRequest, USER_AUTH));

        String expectedMessage = "Applicant solicitor does not belong to chosen applicant organisation";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

}
