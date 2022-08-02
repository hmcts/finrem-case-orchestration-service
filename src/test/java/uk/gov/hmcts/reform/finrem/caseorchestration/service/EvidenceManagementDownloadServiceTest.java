package uk.gov.hmcts.reform.finrem.caseorchestration.service;


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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidUriException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDownloadServiceTest {

    private static final String EVIDENCE_MANAGEMENT_SERVICE_URL = "http://localhost:8080/documents/";
    private static final String URL = "http://dm-store-demo.service.core-compute-demo.internal/";

    @Mock private RestTemplate restTemplate;
    @Mock private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private EvidenceManagementDownloadService downloadService;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(downloadService, "documentManagementStoreUrl", URL);
    }

    @Test
    public void shouldPassThruDocumentDownloadSuccessfullyState() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(URL.concat("/documents/56"), HttpStatus.OK);

        ResponseEntity<?> response = downloadService.download(fileUrl);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfFileDoesNotExsist() {

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("random");
        setupMockEvidenceManagementService(URL.concat("/documents/random"), HttpStatus.NOT_FOUND);

        downloadService.download(fileUrl);
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

        downloadService.download(fileUrl);
        fail("Failed to receive exception resulting from non-running EM service");
    }

    @Test(expected = InvalidUriException.class)
    public void shouldPassThruExceptionThrownWhenInvalidUri() {
        String fileUrl = "//><sssssss/>";
        downloadService.download(fileUrl);
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
