package uk.gov.hmcts.reform.finrem.functional.caseorchestration;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseControllerTest;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@SpringBootTest
@AutoConfigureMockMvc
public class CaseDataTest extends BaseControllerTest {

    private static final String VALID_JSON = "/fixtures/hwf.json";
    private static final String INVALID_JSON = "/fixtures/contested/validate-hearing-successfully.json";

    @Test
    public void shouldSuccessfullySetSolicitorOrgDetails() throws Exception {
        loadRequestContentWith(VALID_JSON);
        mvc.perform(post("/case-orchestration/set-solicitor-organisation-details")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.data.applicantSolicitorFirm", is("FRApplicantSolicitorFirm")))
            .andExpect(jsonPath("$.data.rSolicitorFirm", is("FRApplicantSolicitorFirm")));
    }

    @Test
    public void shouldNotSetSolicitorOrgDetails() throws Exception {
        loadRequestContentWith(INVALID_JSON);
        mvc.perform(post("/case-orchestration/set-solicitor-organisation-details")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.data.applicantSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorFirm").doesNotExist());
    }
}
