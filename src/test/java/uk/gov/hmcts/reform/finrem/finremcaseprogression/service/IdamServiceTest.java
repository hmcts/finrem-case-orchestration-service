package uk.gov.hmcts.reform.finrem.finremcaseprogression.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.FinremCaseProgressionApplication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FinremCaseProgressionApplication.class)
@TestPropertySource("/application.properties")
public class IdamServiceTest {

    @Autowired
    private IdamService idamService;

    private MockRestServiceServer mockServer;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void retrieveUserEmailId() {
        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"email\": \"test@test.com\"}", MediaType.APPLICATION_JSON));

        String userEmailId = idamService.getUserEmailId("azsssfsdffsafa");
        assertThat(userEmailId, is("test@test.com"));
    }

    private String toUri() {
        return "http://test/details";
    }
}