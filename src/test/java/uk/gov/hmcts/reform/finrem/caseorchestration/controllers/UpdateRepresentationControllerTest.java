package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AboutToStartNocCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateRepresentationService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(UpdateRepresentationController.class)
public class UpdateRepresentationControllerTest extends BaseControllerTest {

    private static final String PATH = "/fixtures/noticeOfChange/";
    private static final String VALID_AUTH_TOKEN = AUTH_TOKEN;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private UpdateRepresentationService updateRepresentationService;

    @MockBean
    private AssignCaseAccessService assignCaseAccessService;

    protected String updateEndpoint() {
        return "/case-orchestration/apply-noc-decision";
    }

    protected String aboutToStartEndpoint() {
        return "/case-orchestration/noc/about-to-start";
    }

    String beforeFixture() {
        return PATH + "AppSolReplacing/change-of-representatives-before.json";
    }

    String jsonFixture() {
        return PATH + "noticeOfChangeApplied.json";
    }

    String beforeAppliedFixture() {
        return PATH + "AppSolReplacing/after-update-details.json";
    }


    @Before
    public void setUp() {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    protected OngoingStubbing<AboutToStartOrSubmitCallbackResponse> whenServiceUpdatesRepresentationValid() {
        return when(updateRepresentationService.updateRepresentationAsSolicitor(any(), eq(VALID_AUTH_TOKEN)));
    }

    protected OngoingStubbing<AboutToStartOrSubmitCallbackResponse> whenServiceAssignsCaseAccessValid() {
        return when(assignCaseAccessService.applyDecision(eq(VALID_AUTH_TOKEN), any()));
    }

    protected OngoingStubbing<AboutToStartNocCallbackResponse> whenServicePreparesNoC() {
        return when(assignCaseAccessService.prepareNoC(eq(VALID_AUTH_TOKEN), any()));
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(beforeFixture()).toURI()));
    }

    private Map<String, Object> getUpdatedRepresentationData(String filename) throws IOException {
        objectMapper.registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        InputStream resourceAsStream = getClass().getResourceAsStream(filename);
        return objectMapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails().getData();
    }

    @Test
    public void shouldReturnOkResponseWithValidData() throws Exception {
        doRequestSetUp();

        whenServiceUpdatesRepresentationValid().thenReturn(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(getUpdatedRepresentationData(beforeAppliedFixture()))
            .build());
        whenServiceAssignsCaseAccessValid().thenReturn(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(getUpdatedRepresentationData(jsonFixture()))
            .build());

        mvc.perform(post(updateEndpoint())
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION_HEADER, VALID_AUTH_TOKEN)
            .content(requestContent.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ChangeOfRepresentatives[0].value.by",
                is("Test Applicant Solicitor")))
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy.Organisation.OrganisationID",
                is("A31PTVU")))
            .andExpect(jsonPath("$.data.applicantSolicitorName", is("Test Applicant Solicitor")))
            .andExpect(jsonPath("$.data.applicantSolicitorEmail", is("appsolicitor1@yahoo.com")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void updateRepresentationError400() throws Exception {
        doRequestSetUp();
        mvc.perform(post(updateEndpoint())
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void updateRepresentationHttpError500() throws Exception {
        doRequestSetUp();
        whenServiceUpdatesRepresentationValid().thenThrow(feignError());

        mvc.perform(post(updateEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void aboutToStartShouldInitialiseChangeOrgRequest() throws Exception {
        doRequestSetUp();
        whenServicePreparesNoC().thenReturn(AboutToStartNocCallbackResponse
            .builder()
            .data(getUpdatedRepresentationData(jsonFixture()))
            .build());


        mvc.perform(post(aboutToStartEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, VALID_AUTH_TOKEN)
                .content(requestContent.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.changeOrganisationRequestField.CaseRoleId.value.code",
                is("[APPSOLICITOR]")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }
}
