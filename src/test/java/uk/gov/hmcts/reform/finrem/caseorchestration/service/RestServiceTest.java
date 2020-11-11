package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_VALUE;

public class RestServiceTest extends BaseServiceTest {

    @Autowired private RestService restService;

    @MockBean private RestTemplate restTemplate;

    @Captor private ArgumentCaptor<URI> uriCaptor;
    @Captor private ArgumentCaptor<HttpEntity> authRequestCaptor;

    @Test
    public void restApiPostCall() {
        Map<String, Object> body = new HashMap<>();
        body.put(TEST_KEY, TEST_VALUE);

        restService.restApiPostCall(AUTH_TOKEN, TEST_URL, body);

        Mockito.verify(restTemplate, Mockito.times(1)).exchange(uriCaptor.capture(), eq(HttpMethod.POST), authRequestCaptor.capture(), eq(Map.class));

        HttpHeaders headers = authRequestCaptor.getValue().getHeaders();
        Map<String, Object> requestBody = (Map<String, Object>) authRequestCaptor.getValue().getBody();

        Assert.assertEquals(uriCaptor.getValue(), URI.create(TEST_URL));
        Assert.assertEquals(headers.get(AUTHORIZATION_HEADER).get(0), AUTH_TOKEN);
        Assert.assertEquals(headers.get("Content-Type").get(0), MediaType.APPLICATION_JSON_VALUE);
        Assert.assertEquals(requestBody.get(TEST_KEY), TEST_VALUE);
    }
}