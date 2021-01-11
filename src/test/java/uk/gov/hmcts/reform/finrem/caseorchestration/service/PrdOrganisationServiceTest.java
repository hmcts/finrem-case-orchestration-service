package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;

public class PrdOrganisationServiceTest extends BaseServiceTest {

    @Autowired PrdOrganisationService prdOrganisationService;

    @MockBean RestService restService;
    @MockBean PrdOrganisationConfiguration prdOrganisationConfiguration;

    @Test
    public void whenRetrieveOrganisationData_thenRestTemplateIsCalledWithExpectedParameters() {
        String addressLine1 = "addressLine1";
        String addressLine2 = "addressLine2";
        String addressLine3 = "addressLine3";
        String country = "country";
        String county = "county";
        String postCode = "postCode";
        String townCity = "townCity";

        OrganisationContactInformation contactInformation = OrganisationContactInformation.builder()
            .addressLine1(addressLine1)
            .addressLine2(addressLine2)
            .addressLine3(addressLine3)
            .country(country)
            .county(county)
            .postcode(postCode)
            .townCity(townCity)
            .build();
        OrganisationsResponse restApiGetResponse = OrganisationsResponse.builder().contactInformation(contactInformation).build();

        when(prdOrganisationConfiguration.getOrganisationsUrl()).thenReturn(TEST_URL);
        when(restService.restApiGetCall(eq(AUTH_TOKEN), eq(TEST_URL))).thenReturn(restApiGetResponse);

        OrganisationsResponse organisationsResponse = prdOrganisationService.retrieveOrganisationsData(AUTH_TOKEN);

        assertThat(organisationsResponse.getContactInformation().getAddressLine1(), is(contactInformation.getAddressLine1()));
        assertThat(organisationsResponse.getContactInformation().getAddressLine2(), is(contactInformation.getAddressLine2()));
        assertThat(organisationsResponse.getContactInformation().getAddressLine3(), is(contactInformation.getAddressLine3()));
        assertThat(organisationsResponse.getContactInformation().getCountry(), is(contactInformation.getCountry()));
        assertThat(organisationsResponse.getContactInformation().getCounty(), is(contactInformation.getCounty()));
        assertThat(organisationsResponse.getContactInformation().getPostcode(), is(contactInformation.getPostcode()));
        assertThat(organisationsResponse.getContactInformation().getTownCity(), is(contactInformation.getTownCity()));

        verify(restService, times(1)).restApiGetCall(eq(AUTH_TOKEN), eq(TEST_URL));
        verify(prdOrganisationConfiguration, times(1)).getOrganisationsUrl();
    }
}
