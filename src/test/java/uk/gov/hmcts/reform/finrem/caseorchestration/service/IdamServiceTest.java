package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class IdamServiceTest extends BaseServiceTest {

    @Autowired
    private IdamService idamService;

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