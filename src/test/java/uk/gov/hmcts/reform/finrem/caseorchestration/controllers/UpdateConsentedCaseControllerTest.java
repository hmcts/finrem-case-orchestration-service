package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(UpdateConsentedCaseController.class)
public class UpdateConsentedCaseControllerTest extends BaseControllerTest {

    private static final String UPDATE_CONTACT_DETAILS_ENDPOINT = "/case-orchestration/update-contact-details";
    private static final String FEE_LOOKUP_JSON = "/fixtures/fee-lookup.json";

    private ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode requestContent;

    @MockBean
    private UpdateRepresentationWorkflowService mockNocWorkflowService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void givenValidData_whenUpdateCaseDetails_thenShouldPreserveOrgPolicies() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-divorce-details-d81-joint.json").toURI()));
        mvc.perform(post(UPDATE_CONTACT_DETAILS_ENDPOINT)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy").exists())
            .andExpect(jsonPath("$.data.RespondentOrganisationPolicy").exists());
    }

    @Test
    public void givenValidData_whenUpdateContactDetailsAndIncludesNoC_thenReturnCorrectData() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/noticeOfChange/caseworkerNoc/consented-change-of-reps.json").toURI()));
        when(mockNocWorkflowService.handleNoticeOfChangeWorkflow(any(), any(), any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        mvc.perform(post(UPDATE_CONTACT_DETAILS_ENDPOINT)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldSuccessfullyRemoveApplicantSolicitorDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/noticeOfChange/caseworkerNoc/consented-change-of-reps-for-applicant.json").toURI()));
        when(mockNocWorkflowService.handleNoticeOfChangeWorkflow(any(), any(), any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        mvc.perform(post(UPDATE_CONTACT_DETAILS_ENDPOINT)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

    }

    @Test
    public void shouldSuccessfullyRemoveRespondentDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/noticeOfChange/caseworkerNoc/consented-change-of-reps-for-respondent.json").toURI()));
        when(mockNocWorkflowService.handleNoticeOfChangeWorkflow(any(), any(), any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        mvc.perform(post(UPDATE_CONTACT_DETAILS_ENDPOINT)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldSuccessfullyRemoveRespondentSolicitorDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/noticeOfChange/caseworkerNoc/consented-change-of-reps-no-respondent-change.json").toURI()));
        when(mockNocWorkflowService.handleNoticeOfChangeWorkflow(any(), any(), any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        mvc.perform(post(UPDATE_CONTACT_DETAILS_ENDPOINT)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(FEE_LOOKUP_JSON).toURI()));
    }
}




