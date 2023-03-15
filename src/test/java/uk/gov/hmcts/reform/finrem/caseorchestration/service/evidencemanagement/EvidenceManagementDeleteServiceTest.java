package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EvidenceManagementDeleteServiceTest {

    private static final String EVIDENCE_MANAGEMENT_SERVICE_URL = "http://localhost:8080/documents/";

    @Mock
    IdamAuthService idamAuthService;
    @Mock private RestTemplate restTemplate;
    @Mock private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private EvidenceManagementDeleteService deleteService;

    @Before
    public void setUp() {
        when(idamAuthService.getUserDetails(anyString())).thenReturn(UserDetails.builder().id("19").build());
    }

    /**
     * This test issues a document delete request that is expected to succeed. It ensures that the OK response from
     * the EM document store service passes cleanly through the evidence management client api to the caller
     * without any issues or exceptions occurring.
     * <p/>
     */
    @Test
    public void shouldPassThruDocumentDeletedSuccessfullyState() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.OK);

        ResponseEntity<?> response = deleteService.deleteFile(fileUrl, "AAAABBBB");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    /**
     * This test issues a document delete request that is expected to do nothing. It ensures that the NO_CONTENT
     * response from the EM document store service passes cleanly through the evidence management client api to
     * the caller without any issues or exceptions occurring.
     * <p/>
     */
    @Test
    public void shouldPassThruNoDocumentIdIsPassedState() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.NO_CONTENT);

        ResponseEntity<?> response = deleteService.deleteFile(fileUrl, "AAAABBBB");

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }


    /**
     * This test issues a document delete request that is expected to be rejected due to the caller being unauthorised.
     * It ensures that the UNAUTHORIZED response from the EM document store service passes cleanly through the
     * evidence management client api to the caller without any issues or exceptions occurring.
     * <p/>
     */
    @Test
    public void shouldPassThruNotAuthorisedAuthTokenState() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.UNAUTHORIZED);

        ResponseEntity<?> response = deleteService.deleteFile(fileUrl, "CCCCDDDD");

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    /**
     * This test issues a document delete request that is expected to be rejected due to the caller being unauthenticated.
     * It ensures that the FORBIDDEN response from the EM document store service passes cleanly through the
     * evidence management client api to the caller without any issues or exceptions occurring.
     * <p/>
     */
    @Test
    public void shouldPassThruNotAuthenticatedAuthTokenState() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("56");
        setupMockEvidenceManagementService(fileUrl, HttpStatus.FORBIDDEN);

        ResponseEntity<?> response = deleteService.deleteFile(fileUrl, "");

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /**
     * This test issues a document delete request that is expected to cause an exception due to the EM document service
     * being unavailable. It ensures that the expected exception passes cleanly through to the caller.
     * <p/>
     */
    @Test(expected = ResourceAccessException.class)
    public void shouldPassThruExceptionThrownWhenEvidenceManagementServiceNotFound() {
        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("25");

        doThrow(ResourceAccessException.class)
            .when(restTemplate)
            .exchange(Mockito.eq(fileUrl),
                Mockito.eq(HttpMethod.DELETE),
                any(),
                any(Class.class));

        deleteService.deleteFile(fileUrl, "AAAABBBB");

        fail("Failed to receive exception resulting from non-running EM service");
    }

    @Test
    public void shouldCatchExceptionFromUserServiceAndReturnResponseWithSameHttpStatus() {
        Request requestForExceptionInstance = Request.create(Request.HttpMethod.POST, "",
            new HashMap<>(), Request.Body.empty(), new RequestTemplate());
        doThrow(new FeignException.InternalServerError("does not compute",
            requestForExceptionInstance, new byte[] {}, new HashMap()))
            .when(idamAuthService)
            .getUserDetails(anyString());

        String fileUrl = EVIDENCE_MANAGEMENT_SERVICE_URL.concat("25");
        ResponseEntity<String> responseEntity =
            deleteService.deleteFile(fileUrl, "AAAABBBB");

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
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
}
