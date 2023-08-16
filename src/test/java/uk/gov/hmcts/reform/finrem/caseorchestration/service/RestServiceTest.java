package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_USER_ID;

public class RestServiceTest extends BaseServiceTest {

    @Autowired
    private RestService restService;

    @MockBean
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<URI> uriCaptor;
    @Captor
    private ArgumentCaptor<HttpEntity> authRequestCaptor;

    private AssignCaseAccessRequest body;
    private ResponseEntity<Map> mockResponse;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        body = AssignCaseAccessRequest
            .builder()
            .case_id(TEST_CASE_ID)
            .case_type_id(CaseType.CONTESTED.getCcdType())
            .assignee_id(TEST_USER_ID)
            .build();

        mockResponse = ResponseEntity.accepted().build();
    }

    @Test
    public void restApiPostCall() {
        when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(Map.class))).thenReturn(mockResponse);

        restService.restApiPostCall(AUTH_TOKEN, TEST_URL, body);

        assertRestApiCall(HttpMethod.POST);

        assertEquals(authRequestCaptor.getValue().getBody(), body);
    }

    @Test(expected = NullPointerException.class)
    public void restApiPostCall_exception() {
        when(restTemplate.exchange(any(), eq(HttpMethod.POST), any(), eq(Map.class))).thenThrow(NullPointerException.class);

        restService.restApiPostCall(AUTH_TOKEN, TEST_URL, body);
    }

    @Test
    public void restApiDeleteCall() {
        when(restTemplate.exchange(any(), eq(HttpMethod.DELETE), any(), eq(Map.class))).thenReturn(mockResponse);

        restService.restApiDeleteCall(AUTH_TOKEN, TEST_URL, body);

        assertRestApiCall(HttpMethod.DELETE);

        assertEquals(authRequestCaptor.getValue().getBody(), body);
    }

    @Test(expected = NullPointerException.class)
    public void restApiDeleteCall_exception() {
        when(restTemplate.exchange(any(), eq(HttpMethod.DELETE), any(), eq(Map.class))).thenThrow(NullPointerException.class);

        restService.restApiDeleteCall(AUTH_TOKEN, TEST_URL, body);
    }

    @Test
    public void restApiGetCall() {
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(Map.class))).thenReturn(mockResponse);

        Map response = restService.restApiGetCall(AUTH_TOKEN, TEST_URL);

        assertEquals(response, mockResponse.getBody());

        assertRestApiCall(HttpMethod.GET);

        Assert.assertNull(authRequestCaptor.getValue().getBody());
    }

    @Test(expected = NullPointerException.class)
    public void restApiGetCall_exception() {
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(Map.class))).thenThrow(NullPointerException.class);

        restService.restApiGetCall(AUTH_TOKEN, TEST_URL);
    }

    private void assertRestApiCall(HttpMethod httpMethod) {
        verify(restTemplate, times(1)).exchange(uriCaptor.capture(),
            eq(httpMethod), authRequestCaptor.capture(), eq(Map.class));
        verify(authTokenGenerator, times(1)).generate();

        HttpHeaders headers = authRequestCaptor.getValue().getHeaders();

        assertEquals(uriCaptor.getValue(), URI.create(TEST_URL));
        assertEquals(AUTH_TOKEN, headers.get(AUTHORIZATION_HEADER).get(0));
        assertEquals(TEST_SERVICE_TOKEN, headers.get(SERVICE_AUTHORISATION_HEADER).get(0));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, headers.get("Content-Type").get(0));
    }
}