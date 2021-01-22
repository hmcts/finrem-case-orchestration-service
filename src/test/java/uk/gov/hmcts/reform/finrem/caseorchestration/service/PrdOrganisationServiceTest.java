package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.PrdClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SERVICE_TOKEN;

@ActiveProfiles("test-mock-feign-clients")
public class PrdOrganisationServiceTest extends BaseServiceTest {

    @Autowired PrdOrganisationService prdOrganisationService;

    @MockBean PrdConfiguration prdOrganisationConfiguration;
    @MockBean IdamService idamService;
    @Autowired PrdClient prdClientMock;

    private final String username = "username";
    private final String password = "password";

    @Test
    public void whenRetrieveOrganisationData_thenPrdClientCalled() {
        OrganisationsResponse mockResponse = OrganisationsResponse.builder().build();

        when(prdOrganisationConfiguration.getUsername()).thenReturn(username);
        when(prdOrganisationConfiguration.getPassword()).thenReturn(password);
        when(idamService.authenticateUser(username, password)).thenReturn(AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(prdClientMock.getOrganisationById(AUTH_TOKEN, TEST_SERVICE_TOKEN, TEST_CASE_ID)).thenReturn(mockResponse);

        OrganisationsResponse organisationsResponse = prdOrganisationService.retrieveOrganisationsData(TEST_CASE_ID);

        Assert.assertEquals(organisationsResponse, mockResponse);
    }
}
