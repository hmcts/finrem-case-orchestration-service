package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

        Map<String, Object> contactInfoMap = new HashMap<>();
        contactInfoMap.put("addressLine1", addressLine1);
        contactInfoMap.put("addressLine2", addressLine2);
        contactInfoMap.put("addressLine3", addressLine3);
        contactInfoMap.put("country", country);
        contactInfoMap.put("county", county);
        contactInfoMap.put("postCode", postCode);
        contactInfoMap.put("townCity", townCity);

        Map<String, Object> restApiGetResponseBody = new HashMap<>();
        restApiGetResponseBody.put("contactInformation", Arrays.asList(contactInfoMap));

        when(prdOrganisationConfiguration.getOrganisationsUrl()).thenReturn(TEST_URL);
        when(restService.restApiGetCall(eq(AUTH_TOKEN), eq(TEST_URL))).thenReturn(restApiGetResponseBody);

        OrganisationsResponse organisationsResponse = prdOrganisationService.retrieveOrganisationsData(AUTH_TOKEN);

        assertThat(organisationsResponse.getContactInformation().get(0).getAddressLine1(), is(addressLine1));
        assertThat(organisationsResponse.getContactInformation().get(0).getAddressLine2(), is(addressLine2));
        assertThat(organisationsResponse.getContactInformation().get(0).getAddressLine3(), is(addressLine3));
        assertThat(organisationsResponse.getContactInformation().get(0).getCountry(), is(country));
        assertThat(organisationsResponse.getContactInformation().get(0).getCounty(), is(county));
        assertThat(organisationsResponse.getContactInformation().get(0).getPostcode(), is(postCode));
        assertThat(organisationsResponse.getContactInformation().get(0).getTownCity(), is(townCity));

        verify(restService, times(1)).restApiGetCall(eq(AUTH_TOKEN), eq(TEST_URL));
        verify(prdOrganisationConfiguration, times(1)).getOrganisationsUrl();
    }
}
