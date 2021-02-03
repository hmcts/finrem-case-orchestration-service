package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_REFERENCE;

public class UpdateSolicitorDetailsServiceTest extends BaseServiceTest {

    public static final String ADDRESS_LINE_1 = "addressLine1";
    public static final String ADDRESS_LINE_2 = "addressLine2";
    public static final String ADDRESS_LINE_3 = "addressLine3";
    public static final String COUNTY = "county";
    public static final String COUNTRY = "country";
    public static final String TOWN_CITY = "townCity";
    public static final String POSTCODE = "postCode";

    @Autowired UpdateSolicitorDetailsService updateSolicitorDetailsService;
    @MockBean PrdOrganisationService prdOrganisationService;
    @MockBean CaseDataService caseDataService;

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
    public void setUp() {
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(prdOrganisationService.retrieveOrganisationsData(eq(AUTH_TOKEN))).thenReturn(OrganisationsResponse.builder()
            .contactInformation(Arrays.asList(organisationContactInformation))
            .name(TEST_SOLICITOR_NAME)
            .organisationIdentifier(TEST_SOLICITOR_REFERENCE)
            .build());
    }

    @Test
    public void shouldSuccessfullySetApplicantSolicitorOrganisationDetailsContested() {
        CaseDetails caseDetails = buildCaseDetails();

        updateSolicitorDetailsService.setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, caseDetails);

        Map<String, Object> addressMap = (Map<String, Object>) caseDetails.getData().get(CONTESTED_SOLICITOR_ADDRESS);

        Assert.assertEquals(addressMap.get("AddressLine1"), organisationContactInformation.getAddressLine1());
        Assert.assertEquals(addressMap.get("AddressLine2"), organisationContactInformation.getAddressLine2());
        Assert.assertEquals(addressMap.get("AddressLine3"), organisationContactInformation.getAddressLine3());
        Assert.assertEquals(addressMap.get("County"), organisationContactInformation.getCounty());
        Assert.assertEquals(addressMap.get("Country"), organisationContactInformation.getCountry());
        Assert.assertEquals(addressMap.get("PostTown"), organisationContactInformation.getTownCity());
        Assert.assertEquals(addressMap.get("PostCode"), organisationContactInformation.getPostcode());
        Assert.assertEquals(caseDetails.getData().get(CONTESTED_SOLICITOR_FIRM), TEST_SOLICITOR_NAME);
        Assert.assertEquals(caseDetails.getData().get(SOLICITOR_REFERENCE), TEST_SOLICITOR_REFERENCE);
    }

    @Test
    public void shouldSuccessfullySetApplicantSolicitorOrganisationDetailsConsented() {
        CaseDetails caseDetails = buildCaseDetails();

        when(caseDataService.isContestedApplication(caseDetails)).thenReturn(false);

        updateSolicitorDetailsService.setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, caseDetails);

        Map<String, Object> addressMap = (Map<String, Object>) caseDetails.getData().get(CONSENTED_SOLICITOR_ADDRESS);

        Assert.assertEquals(addressMap.get("AddressLine1"), organisationContactInformation.getAddressLine1());
        Assert.assertEquals(addressMap.get("AddressLine2"), organisationContactInformation.getAddressLine2());
        Assert.assertEquals(addressMap.get("AddressLine3"), organisationContactInformation.getAddressLine3());
        Assert.assertEquals(addressMap.get("County"), organisationContactInformation.getCounty());
        Assert.assertEquals(addressMap.get("Country"), organisationContactInformation.getCountry());
        Assert.assertEquals(addressMap.get("PostTown"), organisationContactInformation.getTownCity());
        Assert.assertEquals(addressMap.get("PostCode"), organisationContactInformation.getPostcode());
        Assert.assertEquals(caseDetails.getData().get(CONSENTED_SOLICITOR_FIRM), TEST_SOLICITOR_NAME);
        Assert.assertEquals(caseDetails.getData().get(SOLICITOR_REFERENCE), TEST_SOLICITOR_REFERENCE);
    }

    @Test
    public void shouldNotSetApplicantSolicitorOrganisationDetails_orgRespNull() {
        when(prdOrganisationService.retrieveOrganisationsData(eq(AUTH_TOKEN))).thenReturn(null);

        CaseDetails caseDetails = buildCaseDetails();

        updateSolicitorDetailsService.setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, caseDetails);

        Assert.assertFalse(caseDetails.getData().containsKey(CONTESTED_SOLICITOR_ADDRESS));
        Assert.assertFalse(caseDetails.getData().containsKey(CONTESTED_SOLICITOR_FIRM));
        Assert.assertFalse(caseDetails.getData().containsKey(SOLICITOR_REFERENCE));
    }
}
