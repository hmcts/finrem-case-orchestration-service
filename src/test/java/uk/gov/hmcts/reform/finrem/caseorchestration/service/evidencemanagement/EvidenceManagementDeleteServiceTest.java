package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.FILE_URL;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDeleteServiceTest {

    public static final String IDAM_OAUTH_TOKEN = "idamOauthToken";
    public static final String SERVICE_AUTH = "serviceAuth";
    public static final String DOC_UUID = "d607c045-878e-475f-ab8e-b2f667d8af64";

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CaseDocumentClient caseDocumentClient;
    @Mock
    private IdamAuthService idamAuthService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private EvidenceManagementDeleteService emDeleteService;
    private IdamToken idamToken;

    @Before
    public void setUp() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(false);
        idamToken = IdamToken.builder()
            .idamOauth2Token(IDAM_OAUTH_TOKEN)
            .serviceAuthorization(SERVICE_AUTH)
            .build();
        when(idamAuthService.getIdamToken(any())).thenReturn(idamToken);
        when(idamAuthService.getUserDetails(anyString())).thenReturn(UserDetails.builder().id("19").build());
    }

    @Test
    public void shouldPassThruDocumentDeletedSuccessfullyState() {
        setupMockEvidenceManagementService(FILE_URL, HttpStatus.OK);

        emDeleteService.delete(FILE_URL, "AAAABBBB");
        verify(idamAuthService).getUserDetails(anyString());
        verify(restTemplate).exchange(eq(FILE_URL), eq(HttpMethod.DELETE), any(), eq(String.class));
    }

    /**
     * This test issues a document delete request that is expected to be rejected due to the caller being unauthorised.
     * It ensures that the UNAUTHORIZED response from the EM document store service passes cleanly through the
     * evidence management client api to the caller without any issues or exceptions occurring.
     * <p/>
     */
    @Test
    public void shouldPassThruNotAuthorisedAuthTokenState() {
        setupMockEvidenceManagementService(FILE_URL, HttpStatus.UNAUTHORIZED);

        emDeleteService.delete(FILE_URL, "CCCCDDDD");

        verify(idamAuthService).getUserDetails(anyString());
        verify(restTemplate).exchange(eq(FILE_URL), eq(HttpMethod.DELETE), any(), eq(String.class));
    }

    /**
     * This test issues a document delete request that is expected to be rejected due to the caller being unauthenticated.
     * It ensures that the FORBIDDEN response from the EM document store service passes cleanly through the
     * evidence management client api to the caller without any issues or exceptions occurring.
     * <p/>
     */
    @Test
    public void shouldPassThruNotAuthenticatedAuthTokenState() {
        setupMockEvidenceManagementService(FILE_URL, HttpStatus.FORBIDDEN);

        emDeleteService.delete(FILE_URL, "");

        verify(idamAuthService).getUserDetails(anyString());
        verify(restTemplate).exchange(eq(FILE_URL), eq(HttpMethod.DELETE), any(), eq(String.class));
    }

    /**
     * This test issues a document delete request that is expected to cause an exception due to the EM document service
     * being unavailable. It ensures that the expected exception passes cleanly through to the caller.
     * <p/>
     */
    @Test(expected = ResourceAccessException.class)
    public void shouldPassThruExceptionThrownWhenEvidenceManagementServiceNotFound() {

        doThrow(ResourceAccessException.class)
            .when(restTemplate)
            .exchange(Mockito.eq(FILE_URL),
                Mockito.eq(HttpMethod.DELETE),
                any(),
                any(Class.class));

        emDeleteService.delete(FILE_URL, "AAAABBBB");

        fail("Failed to receive exception resulting from non-running EM service");
    }

    /**
     * This method sets up the mock evidence management document service endpoint for the currently executing test.
     * <p/>
     *
     * @param fileUrl    a String containing the url for which the mock endpoint will respond
     * @param httpStatus an HttpStatus enum representing the http status value to be returned from the mock endpoint
     */
    private void setupMockEvidenceManagementService(String fileUrl, HttpStatus httpStatus) {
        when(authTokenGenerator.generate()).thenReturn("xxxx");

        doReturn(new ResponseEntity<>(httpStatus))
            .when(restTemplate)
            .exchange(Mockito.eq(fileUrl),
                Mockito.eq(HttpMethod.DELETE),
                any(),
                any(Class.class));
    }

    @Test
    public void givenUploadResponseReturned_whenUploadIsCalled_thenExpectUploadToSucceed() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        emDeleteService.delete(FILE_URL, AUTH_TOKEN);

        verify(idamAuthService, times(1)).getIdamToken(AUTH_TOKEN);
        verify(caseDocumentClient, times(1))
            .deleteDocument(IDAM_OAUTH_TOKEN, SERVICE_AUTH, UUID.fromString(DOC_UUID),true);
    }
}
