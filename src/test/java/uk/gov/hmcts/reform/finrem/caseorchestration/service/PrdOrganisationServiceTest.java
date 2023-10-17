package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.OrganisationApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationUser;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;

@RunWith(MockitoJUnitRunner.class)
public class PrdOrganisationServiceTest extends BaseServiceTest {

    public static final String USER_ID = "someUserId";
    public static final String TEST_EMAIL = "test@gmail.com";

    @InjectMocks
    private PrdOrganisationService prdOrganisationService;

    @Mock
    private RestService restService;
    @Mock
    private PrdOrganisationConfiguration prdOrganisationConfiguration;
    @Mock
    private OrganisationApi organisationApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

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
        restApiGetResponseBody.put("contactInformation", List.of(contactInfoMap));

        when(prdOrganisationConfiguration.getOrganisationsUrl()).thenReturn(TEST_URL);
        when(restService.restApiGetCall(AUTH_TOKEN, TEST_URL)).thenReturn(restApiGetResponseBody);

        OrganisationsResponse organisationsResponse = prdOrganisationService.retrieveOrganisationsData(AUTH_TOKEN);

        assertThat(organisationsResponse.getContactInformation().get(0).getAddressLine1(), is(addressLine1));
        assertThat(organisationsResponse.getContactInformation().get(0).getAddressLine2(), is(addressLine2));
        assertThat(organisationsResponse.getContactInformation().get(0).getAddressLine3(), is(addressLine3));
        assertThat(organisationsResponse.getContactInformation().get(0).getCountry(), is(country));
        assertThat(organisationsResponse.getContactInformation().get(0).getCounty(), is(county));
        assertThat(organisationsResponse.getContactInformation().get(0).getPostcode(), is(postCode));
        assertThat(organisationsResponse.getContactInformation().get(0).getTownCity(), is(townCity));

        verify(restService, times(1)).restApiGetCall(AUTH_TOKEN, TEST_URL);
        verify(prdOrganisationConfiguration, times(1)).getOrganisationsUrl();
    }

    @Test
    public void givenRegisteredUser_whenFindUserByEmail_thenReturnUserIdOptional() {
        OrganisationUser user = OrganisationUser.builder().userIdentifier(USER_ID).build();
        when(organisationApi.findUserByEmail(eq(AUTH_TOKEN), any(), eq(TEST_EMAIL))).thenReturn(user);

        Optional<String> userId = prdOrganisationService.findUserByEmail(TEST_EMAIL, AUTH_TOKEN);

        assertTrue(userId.isPresent());
        assertThat(userId.get(), is(USER_ID));
    }

    @Test
    public void givenUnregisteredUser_whenFindUserByEmail_thenHandleNotFoundException() {
        when(organisationApi.findUserByEmail(eq(AUTH_TOKEN), any(), eq(TEST_EMAIL)))
            .thenThrow(FeignException.NotFound.class);

        Optional<String> userId = prdOrganisationService.findUserByEmail(TEST_EMAIL, AUTH_TOKEN);

        assertTrue(userId.isEmpty());
    }

    @Test
    public void givenUnregisteredUser_whenFindUserByEmailAndEmailIsNull_thenHandleNotFoundException() {
        when(organisationApi.findUserByEmail(eq(AUTH_TOKEN), any(), eq(TEST_EMAIL)))
            .thenThrow(FeignException.class);
        try {
            prdOrganisationService.findUserByEmail(TEST_EMAIL, AUTH_TOKEN);
        } catch (RuntimeException e) {
            if (e instanceof FeignException) {
                assertEquals("expecting exception to throw when user not found in am",
                    "Email is not valid or null", e.getMessage());
            }
        }
    }
}
