package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CaseFlagsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory.createObjectMapper;

@ExtendWith(MockitoExtension.class)
class CreateCaseServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private CaseFlagsConfiguration caseFlagsConfiguration;

    @InjectMocks
    private CreateCaseService createCaseService;

    private final ObjectMapper objectMapper = createObjectMapper();

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        when(caseFlagsConfiguration.getHmctsId()).thenReturn("financial-remedy");
        when(coreCaseDataApi.submitSupplementaryData(any(), any(), any(), any())).thenReturn(getCase());
    }

    @Test
    void givenCallbackRequest_whenSetSupplementaryData_thenCallCcdSubmitSupplementaryData() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();

        createCaseService.setSupplementaryData(callbackRequest, AUTH_TOKEN);

        verify(coreCaseDataApi,
            times(1)).submitSupplementaryData(any(), any(), any(), any());
    }

    @Test
    void givenFinremCallbackRequest_whenSetSupplementaryData_thenCallCcdSubmitSupplementaryData() {
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(FinremCaseDetails.builder().build()).build();

        createCaseService.setSupplementaryData(callbackRequest, AUTH_TOKEN);

        verify(coreCaseDataApi,
            times(1)).submitSupplementaryData(any(), any(), any(), any());
    }

    private CaseDetails getCase() {
        try (InputStream resourceAsStream = getClass()
            .getResourceAsStream("/fixtures/contested/validate-hearing-with-fastTrackDecision.json")) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
