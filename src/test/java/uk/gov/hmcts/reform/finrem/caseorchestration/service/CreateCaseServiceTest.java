package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CaseFlagsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class CreateCaseServiceTest {

    public static final String A_VALID_SERVICE_TOKEN = "A valid TOKEN";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private CaseFlagsConfiguration caseFlagsConfiguration;

    @InjectMocks
    private CreateCaseService createCaseService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(A_VALID_SERVICE_TOKEN);
        when(caseFlagsConfiguration.getHmctsId()).thenReturn("financial-remedy");
        when(coreCaseDataApi.submitSupplementaryData(any(), any(), any(), any())).thenReturn(getCase());
    }

    @Test
    public void givenCallbackRequest_whenSetSupplementaryData_thenCallCcdSubmitSupplementaryData() {
        CallbackRequest callbackRequest =
            CallbackRequest.builder().caseDetails(getCase()).build();

        createCaseService.setSupplementaryData(callbackRequest, AUTH_TOKEN);

        verify(coreCaseDataApi,
            times(1)).submitSupplementaryData(any(), any(), any(), any());
    }


    @Test
    public void givenFinremCallbackRequest_whenSetSupplementaryData_thenCallCcdSubmitSupplementaryData() {
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