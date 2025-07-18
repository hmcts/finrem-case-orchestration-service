package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@SpringBootTest
@AutoConfigureMockMvc
public class UpdateRepresentationControllerTest extends BaseControllerTest {

    private static final String PATH = "/fixtures/noticeOfChange/";
    private static final String NO_ORG_POLICIES_JSON = "no-org-policies.json";
    private static final String VALID_AUTH_TOKEN = AUTH_TOKEN;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UpdateRepresentationService updateRepresentationService;

    @MockitoBean
    private AssignCaseAccessService assignCaseAccessService;

    @Autowired
    private UpdateRepresentationController updateRepresentationController;

    @InjectMocks
    private UpdateRepresentationWorkflowService updateRepresentationWorkflowService;

    protected String updateEndpoint() {
        return "/case-orchestration/apply-noc-decision";
    }

    protected String setDefaultsEndpoint() {
        return "/case-orchestration/set-update-defaults";
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

    protected OngoingStubbing<Map<String, Object>> whenServiceUpdatesRepresentationValid() {
        return when(updateRepresentationService.updateRepresentationAsSolicitor(any(), eq(VALID_AUTH_TOKEN)));
    }

    protected OngoingStubbing<AboutToStartOrSubmitCallbackResponse> whenServiceAssignsCaseAccessValid() {
        return when(assignCaseAccessService.applyDecision(eq(VALID_AUTH_TOKEN), any()));
    }

    protected OngoingStubbing<CaseAssignmentUserRolesResponse> whenRevokeCreatorCaseAccessValid() {
        return when(assignCaseAccessService.findAndRevokeCreatorRole(any()));
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

        whenServiceUpdatesRepresentationValid().thenReturn(getUpdatedRepresentationData(beforeAppliedFixture()));
        whenServiceAssignsCaseAccessValid().thenReturn(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(getUpdatedRepresentationData(jsonFixture()))
            .build());
        whenRevokeCreatorCaseAccessValid().thenReturn(null);

        mvc.perform(post(updateEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, VALID_AUTH_TOKEN)
                .content(requestContent.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.RepresentationUpdateHistory[0].value.by",
                is("Test Applicant Solicitor")))
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy.Organisation.OrganisationID",
                is("A31PTVU")))
            .andExpect(jsonPath("$.data.applicantSolicitorName", is("Test Applicant Solicitor")))
            .andExpect(jsonPath("$.data.applicantSolicitorEmail", is("appsolicitor1@yahoo.com")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));

        verify(updateRepresentationService, times(1)).addRemovedSolicitorOrganisationFieldToCaseData(any(CaseDetails.class));
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
}
