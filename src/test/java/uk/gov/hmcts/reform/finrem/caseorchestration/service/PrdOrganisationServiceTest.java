package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.net.URI;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class PrdOrganisationServiceTest extends BaseServiceTest {

    @Autowired PrdOrganisationService prdOrganisationService;
    @MockBean RestTemplate restTemplate;

    @Test
    public void whenRetrieveOrganisationData_thenRestTemplateIsCalledWithExpectedParameters() {
        when(restTemplate.exchange(any(), any(), any(), eq(OrganisationsResponse.class)))
            .thenReturn(ResponseEntity.of(Optional.of(OrganisationsResponse.builder().build())));

        OrganisationsResponse organisationsResponse = prdOrganisationService.retrieveOrganisationsData(AUTH_TOKEN);
        assertThat(organisationsResponse, is(notNullValue()));

        String prdUrlAsPerTestProperties = "http://localhost:8090/refdata/external/v1/organisations";
        verify(restTemplate, times(1)).exchange(eq(URI.create(prdUrlAsPerTestProperties)), eq(HttpMethod.GET),
            any(), eq(OrganisationsResponse.class));
    }
}
