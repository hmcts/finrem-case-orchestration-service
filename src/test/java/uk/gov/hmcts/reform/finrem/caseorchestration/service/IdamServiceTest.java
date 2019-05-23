package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class IdamServiceTest  {

    @Autowired
    private IdamService idamService;

    @Autowired
    protected RestTemplate restTemplate;

    protected MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }
    @Test
    public void retrieveUserRoleIsAdmin() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"roles\": [\"caseworker-divorce-financialremedy-courtadmin\"]}",
            MediaType.APPLICATION_JSON));

        boolean userEmailId = idamService.isUserRoleAdmin("azsssfsdffsafa");
        assertThat(userEmailId, is(Boolean.TRUE));
    }

    @Test
    public void retrieveUserRoleIsNotAdmin() {
        mockServer.expect(requestTo(toUri()))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"roles\": [\"caseworker-divorce-financialremedy-solicitor\"]}",
                MediaType.APPLICATION_JSON));

        boolean userEmailId = idamService.isUserRoleAdmin("azsssfsdffsafa");
        assertThat(userEmailId, is(Boolean.FALSE));
    }

    private String toUri() {
        return "http://localhost:4501/details";
    }
}