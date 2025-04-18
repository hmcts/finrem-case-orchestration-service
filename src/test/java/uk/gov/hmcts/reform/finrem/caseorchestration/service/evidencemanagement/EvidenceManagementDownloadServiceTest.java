package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidUriException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_DOCUMENT_BINARY_URL;

@ExtendWith(MockitoExtension.class)
class EvidenceManagementDownloadServiceTest {

    private static final String EVIDENCE_MANAGEMENT_SERVICE_URL = "http://localhost:8080/documents/";
    private static final String URL = "http://dm-store-demo.service.core-compute-demo.internal/";
    private static final String DOC_BINARY = "http://dm-store:8080/documents/d607c045-878e-475f-ab8e-b2f667d8af64/binary";
    private static final String IDAM_OAUTH_TOKEN = "idamOauthToken";
    private static final String SERVICE_AUTH = "serviceAuth";

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
    @Spy
    private EvidenceManagementDownloadService downloadService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(downloadService, "documentManagementStoreUrl", URL);
        ReflectionTestUtils.setField(downloadService, "featureToggleService", featureToggleService);
        IdamToken idamToken = IdamToken.builder()
            .idamOauth2Token(IDAM_OAUTH_TOKEN)
            .serviceAuthorization(SERVICE_AUTH)
            .build();
        lenient().when(idamAuthService.getIdamToken(any())).thenReturn(idamToken);
    }

    @Test
    void shouldPassThruDocumentDownloadSuccessfullyState() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(URL.concat("/documents/56"), HttpStatus.OK);

        ResponseEntity<Resource> response = downloadService.downloadInResponseEntity(fileUrl, AUTH_TOKEN);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionIfFileDoesNotExist() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("random");
        setupMockEvidenceManagementService(URL.concat("/documents/random"), HttpStatus.NOT_FOUND);

        assertThrows(RuntimeException.class, () -> {
            downloadService.download(fileUrl, AUTH_TOKEN);
        }, "Failed to receive exception");
    }

    @Test
    void shouldPassThruExceptionThrownWhenEvidenceManagementServiceNotFound() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("25");

        doThrow(ResourceAccessException.class)
            .when(restTemplate)
            .exchange(Mockito.eq(URL.concat("/documents/25")),
                Mockito.eq(HttpMethod.GET),
                any(),
                any(Class.class));
        assertThrows(ResourceAccessException.class, () -> {
            downloadService.download(fileUrl, AUTH_TOKEN);
        }, "Failed to receive exception resulting from non-running EM service");
    }

    @Test
    void shouldPassThruExceptionThrownWhenInvalidUri() {
        String fileUrl = "//><sssssss/>";
        assertThrows(InvalidUriException.class, () -> {
            downloadService.download(fileUrl, AUTH_TOKEN);
        });
    }

    @Test
    void shouldCallCaseDocumentClient() {
        when(featureToggleService.isSecureDocEnabled()).thenReturn(true);
        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok().build());

        byte[] response = downloadService.download(DOC_BINARY, AUTH_TOKEN);

        assertNotNull(response);
        verify(caseDocumentClient, times(1))
            .getDocumentBinary(IDAM_OAUTH_TOKEN, SERVICE_AUTH, DOC_BINARY);
    }

    @Test
    void shouldReturnByteArrayWhenDownloadSucceeds() {
        // Given
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl(TEST_DOCUMENT_BINARY_URL).build();
        byte[] expectedBytes = "test content".getBytes();
        ByteArrayResource resource = new ByteArrayResource(expectedBytes);
        ResponseEntity<Resource> responseEntity = new ResponseEntity<>(resource, HttpStatus.OK);

        doReturn(responseEntity) // Use doReturn() for spies
            .when(downloadService)
            .downloadInResponseEntity(TEST_DOCUMENT_BINARY_URL, AUTH_TOKEN);

        // When
        byte[] result = downloadService.getByteArray(caseDocument, AUTH_TOKEN);

        // Then
        assertArrayEquals(expectedBytes, result);
    }

    @Test
    void shouldThrowExceptionWhenDownloadFails() {
        // Given
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl(TEST_DOCUMENT_BINARY_URL).build();
        ResponseEntity<Resource> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        doReturn(responseEntity) // Use doReturn() for spies
            .when(downloadService)
            .downloadInResponseEntity(TEST_DOCUMENT_BINARY_URL, AUTH_TOKEN);

        // When & Then
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
            () -> downloadService.getByteArray(caseDocument, AUTH_TOKEN));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldReturnEmptyByteArrayWhenResourceIsNull() {
        // Given
        CaseDocument caseDocument = CaseDocument.builder().documentBinaryUrl(TEST_DOCUMENT_BINARY_URL).build();
        ResponseEntity<Resource> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        doReturn(responseEntity) // Use doReturn() for spies
            .when(downloadService)
            .downloadInResponseEntity(TEST_DOCUMENT_BINARY_URL, AUTH_TOKEN);

        // When
        byte[] result = downloadService.getByteArray(caseDocument, AUTH_TOKEN);

        // Then
        assertArrayEquals(new byte[0], result);
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
