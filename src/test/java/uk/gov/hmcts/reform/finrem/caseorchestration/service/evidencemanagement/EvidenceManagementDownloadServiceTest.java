package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidUriException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDownloadServiceTest {

    private static final String EVIDENCE_MANAGEMENT_SERVICE_URL = "http://localhost:8080/documents/";
    private static final String URL = "http://dm-store-demo.service.core-compute-demo.internal/";

    public static final String DOC_BINARY =
        "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64/binary";
    public static final String AUTH = "auth";
    public static final String IDAM_OAUTH_TOKEN = "idamOauthToken";
    public static final String SERVICE_AUTH = "serviceAuth";
    private IdamToken idamToken;

    @Mock
    private IdamAuthService idamAuthService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseDocumentClient caseDocumentClient;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private EvidenceManagementDownloadService downloadService;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(downloadService, "documentManagementStoreUrl", URL);
        idamToken = IdamToken.builder()
            .idamOauth2Token(IDAM_OAUTH_TOKEN)
            .serviceAuthorization(SERVICE_AUTH)
            .build();
        when(idamAuthService.getIdamToken(any())).thenReturn(idamToken);
        when(featureToggleService.isSecureDocEnabled()).thenReturn(false);
    }

    @Test
    public void shouldPassThruDocumentDownloadSuccessfullyState() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(URL.concat("/documents/56"), HttpStatus.OK);

        ResponseEntity<Resource> response = downloadService.downloadInResponseEntity(fileUrl, AUTH);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfFileDoesNotExsist() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("random");
        setupMockEvidenceManagementService(URL.concat("/documents/random"), HttpStatus.NOT_FOUND);

        downloadService.download(fileUrl, AUTH);
        fail("Failed to receive exception ");
    }

    @Test(expected = ResourceAccessException.class)
    public void shouldPassThruExceptionThrownWhenEvidenceManagementServiceNotFound() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("25");

        doThrow(ResourceAccessException.class)
            .when(restTemplate)
            .exchange(Mockito.eq(URL.concat("/documents/25")),
                Mockito.eq(HttpMethod.GET),
                any(),
                any(Class.class));

        downloadService.download(fileUrl, AUTH);
        fail("Failed to receive exception resulting from non-running EM service");
    }

    @Test(expected = InvalidUriException.class)
    public void shouldPassThruExceptionThrownWhenInvalidUri() {
        String fileUrl = "//><sssssss/>";
        downloadService.download(fileUrl, AUTH);
    }


    @Test
    public void shouldCallCaseDocumentClient() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok().build());

        byte[] response = downloadService.download(DOC_BINARY, AUTH);

        assertNotNull(response);
        verify(caseDocumentClient, times(1))
            .getDocumentBinary(IDAM_OAUTH_TOKEN, SERVICE_AUTH, DOC_BINARY);
    }

    private void setupMockEvidenceManagementService(String fileUrl, HttpStatus httpStatus) {
        when(authTokenGenerator.generate()).thenReturn("xxxx");

        doReturn(new ResponseEntity<>(httpStatus))
            .when(restTemplate)
            .exchange(Mockito.eq(fileUrl),
                Mockito.eq(HttpMethod.GET),
                any(),
                any(Class.class));
    }
}
