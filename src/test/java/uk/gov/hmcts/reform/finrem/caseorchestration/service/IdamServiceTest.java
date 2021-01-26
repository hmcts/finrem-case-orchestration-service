package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
@ActiveProfiles("test-mock-feign-clients")
public class IdamServiceTest extends BaseServiceTest {
    @Autowired private IdamService idamService;

    @Autowired protected RestTemplate restTemplate;

    @MockBean private IdamClient idamClient;

    private MockRestServiceServer mockServer;

    private String username = "username";
    private String password = "password";

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void retrieveUserRoleIsAdmin() {
        mockServer.expect(requestTo(toUri()))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"roles\": [\"caseworker-divorce-financialremedy-courtadmin\"]}", MediaType.APPLICATION_JSON));

        boolean userEmailId = idamService.isUserRoleAdmin(AUTH_TOKEN);
        assertThat(userEmailId, is(Boolean.TRUE));
    }

    @Test
    public void retrieveUserRoleIsNotAdmin() {
        mockServer.expect(requestTo(toUri()))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"roles\": [\"caseworker-divorce-financialremedy-solicitor\"]}", MediaType.APPLICATION_JSON));

        boolean userEmailId = idamService.isUserRoleAdmin(AUTH_TOKEN);
        assertThat(userEmailId, is(Boolean.FALSE));
    }

    @Test
    public void retrieveUserId() {
        mockServer.expect(requestTo(toUri()))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"id\": \"1234\"}", MediaType.APPLICATION_JSON));

        Assert.assertEquals(idamService.getIdamUserId(AUTH_TOKEN), "1234");
    }

    @Test
    public void authenticateUser() {
        idamService.authenticateUser(username, password);
        Mockito.verify(idamClient, Mockito.times(1)).getAccessToken(username, password);
    }

    private String toUri() {
        return "http://localhost:8080/details";
    }
}