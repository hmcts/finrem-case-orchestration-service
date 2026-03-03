package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.OrganisationApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.MaskHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationUser;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

@ExtendWith(MockitoExtension.class)
class PrdOrganisationServiceTest {

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
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        lenient().when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
    }

    @Test
    void whenRetrieveOrganisationData_thenRestTemplateIsCalledWithExpectedParameters() {
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

        assertThat(organisationsResponse.getContactInformation().getFirst())
            .extracting(
                OrganisationContactInformation::getAddressLine1,
                OrganisationContactInformation::getAddressLine2,
                OrganisationContactInformation::getAddressLine3,
                OrganisationContactInformation::getCountry,
                OrganisationContactInformation::getCounty,
                OrganisationContactInformation::getPostcode,
                OrganisationContactInformation::getTownCity)
                .contains(addressLine1, addressLine2, addressLine3, country, county, townCity);

        verify(restService).restApiGetCall(AUTH_TOKEN, TEST_URL);
        verify(prdOrganisationConfiguration).getOrganisationsUrl();
        verifyNoMoreInteractions(restService, prdOrganisationConfiguration);
    }

    @Test
    void givenRegisteredUser_whenFindUserByEmail_thenReturnExpectedUserId() {
        OrganisationUser user = OrganisationUser.builder().userIdentifier(TEST_USER_ID).build();
        when(organisationApi.findUserByEmail(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_SOLICITOR_EMAIL)).thenReturn(user);

        Optional<String> userId = prdOrganisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, AUTH_TOKEN);

        assertThat(userId).contains(TEST_USER_ID);
        verify(organisationApi).findUserByEmail(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_SOLICITOR_EMAIL);
    }

    @Test
    void givenUnregisteredUser_whenFindUserByEmail_thenReturnEmpty() {
        when(organisationApi.findUserByEmail(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_SOLICITOR_EMAIL))
            .thenThrow(FeignException.NotFound.class);

        Optional<String> userId = prdOrganisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, AUTH_TOKEN);
        assertThat(userId).isEmpty();

        verify(organisationApi).findUserByEmail(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_SOLICITOR_EMAIL);
    }

    @Test
    void givenEmailIsNull_whenOrganisationApiThrowsFeignException_thenThrowRuntimeException() {
        when(organisationApi.findUserByEmail(AUTH_TOKEN, TEST_SERVICE_TOKEN, null))
            .thenThrow(FeignException.class);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            prdOrganisationService.findUserByEmail(null, AUTH_TOKEN));
        assertThat(exception.getMessage())
            .isEqualTo("Email is not valid or null");
        verify(organisationApi).findUserByEmail(AUTH_TOKEN, TEST_SERVICE_TOKEN, null);
    }

    @Test
    void givenEmailIsProvided_whenOrganisationApiThrowsFeignException_thenThrowRuntimeException() {
        when(organisationApi.findUserByEmail(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_SOLICITOR_EMAIL))
            .thenThrow(FeignException.class);

        try (MockedStatic<ExceptionUtils> mockedExceptionUtils = mockStatic(ExceptionUtils.class);
             MockedStatic<MaskHelper> mockedMaskHelper = mockStatic(MaskHelper.class)) {
            ArgumentCaptor<FeignException> captor = ArgumentCaptor.forClass(FeignException.class);

            String stackTrace = "THIS IS A STACK TRACE";
            mockedExceptionUtils.when(() -> ExceptionUtils.getStackTrace(any())).thenReturn(stackTrace);
            mockedMaskHelper.when(() -> MaskHelper.maskEmail(stackTrace, TEST_SOLICITOR_EMAIL))
                .thenReturn("THIS IS A STACK TRACE WITH MASKED EMAIL");

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                prdOrganisationService.findUserByEmail(TEST_SOLICITOR_EMAIL, AUTH_TOKEN));

            mockedExceptionUtils.verify(() -> ExceptionUtils.getStackTrace(captor.capture()));

            assertThat(exception.getMessage())
                .isEqualTo("THIS IS A STACK TRACE WITH MASKED EMAIL");
            verify(organisationApi).findUserByEmail(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_SOLICITOR_EMAIL);
        }
    }

    @Test
    void givenRegisteredUser_whenFindOrganisationIdByUserId_thenReturnExpectedOrganisationId() {
        OrganisationsResponse mockedResponse = mock(OrganisationsResponse.class);
        when(mockedResponse.getOrganisationIdentifier()).thenReturn(TEST_ORG_ID);
        when(organisationApi.findOrganisationDetailsByUserr(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID))
            .thenReturn(mockedResponse);

        // Act
        Optional<String> organisationId = prdOrganisationService.findOrganisationIdByUserId(TEST_USER_ID, AUTH_TOKEN);

        // Verify
        assertThat(organisationId)
            .isPresent()
            .contains(TEST_ORG_ID);
    }

    @Test
    void givenUnregisteredUser_whenFindOrganisationIdByUserId_thenReturnEmpty() {
        when(organisationApi.findOrganisationDetailsByUserr(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID))
            .thenThrow(FeignException.NotFound.class);

        // Act
        Optional<String> organisationId = prdOrganisationService.findOrganisationIdByUserId(TEST_USER_ID, AUTH_TOKEN);

        // Verify
        assertThat(organisationId).isEmpty();
    }

    @Test
    void givenAnyUserId_whenOrganisationApiThrowsFeignException_thenThrowRuntimeException() {
        when(organisationApi.findOrganisationDetailsByUserr(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID))
            .thenThrow(FeignException.class);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            prdOrganisationService.findOrganisationIdByUserId(TEST_USER_ID, AUTH_TOKEN));
        assertThat(exception.getMessage())
            .isEqualTo("Given user_id is not valid or null");
        verify(organisationApi).findOrganisationDetailsByUserr(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_USER_ID);
    }
}
