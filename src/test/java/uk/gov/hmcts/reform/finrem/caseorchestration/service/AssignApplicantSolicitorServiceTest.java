package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORGANISATION_POLICY_ORGANISATION_ID;

@RunWith(MockitoJUnitRunner.class)
public class AssignApplicantSolicitorServiceTest  {

    private static final long CASE_ID = 1583841721773828L;
    private static final String USER_AUTH = "123456789";

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

    public static final String ADDRESS_LINE_1 = "addressLine1";
    public static final String ADDRESS_LINE_2 = "addressLine2";
    public static final String ADDRESS_LINE_3 = "addressLine3";
    public static final String COUNTY = "county";
    public static final String COUNTRY = "country";
    public static final String TOWN_CITY = "townCity";
    public static final String POSTCODE = "postCode";

    OrganisationContactInformation organisationContactInformation = OrganisationContactInformation.builder()
        .addressLine1(ADDRESS_LINE_1)
        .addressLine2(ADDRESS_LINE_2)
        .addressLine3(ADDRESS_LINE_3)
        .county(COUNTY)
        .country(COUNTRY)
        .townCity(TOWN_CITY)
        .postcode(POSTCODE)
        .build();

    @Before
    public void setUp() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().id(CASE_ID).data(caseData).build();
        when(featureToggleService.isAssignCaseAccessEnabled()).thenReturn(true);
        when(featureToggleService.isUseUserTokenEnabled()).thenReturn(true);

    }

    @Test
    public void shouldAssignApplicantSolicitor() {

        caseDetails.getData().put(ORGANISATION_POLICY_APPLICANT, "Test_Policy_Applicant");
        caseDetails.getData().put(ORGANISATION_POLICY_ORGANISATION, "Test_policy_org");
        caseDetails.getData().put(ORGANISATION_POLICY_ORGANISATION_ID, "1234");

        OrganisationsResponse testOrg = OrganisationsResponse.builder()
            .contactInformation(singletonList(organisationContactInformation))
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier("1234")
            .build();

        when(featureToggleService.isAssignCaseAccessEnabled()).thenReturn(true);
        when(prdOrganisationService.retrieveOrganisationsData(USER_AUTH)).thenReturn(testOrg);

        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        assignApplicantSolicitorService.setApplicantSolicitor(callbackRequest, USER_AUTH);

        verify(assignCaseAccessService).assignCaseAccess(caseDetails, USER_AUTH);
        verify(ccdDataStoreService).removeCreatorRole(caseDetails, USER_AUTH);
    }
}
