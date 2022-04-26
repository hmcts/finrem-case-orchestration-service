package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(NoticeOfChangeController.class)
public class NoticeOfChangeControllerTest extends BaseControllerTest {

    private static final String PATH = "/fixtures/noticeOfChange/caseworkerNoc/";

    protected JsonNode requestContent;
    protected CallbackRequest callbackRequest;
    protected CaseDetails caseDetails;

    @MockBean protected NoticeOfChangeService noticeOfChangeService;
    @MockBean protected AssignCaseAccessService assignCaseAccessService;
    @MockBean protected SystemUserService systemUserService;

    protected String updateEndpoint() {
        return "/case-orchestration/representation-change";
    }

    protected OngoingStubbing<Map<String, Object>> whenServiceUpdatesRepresentation() {
        return when(noticeOfChangeService.caseWorkerUpdatesRepresentation(isA(CaseDetails.class),
            eq(AUTH_TOKEN), any()));
    }

    protected OngoingStubbing<AboutToStartOrSubmitCallbackResponse> whenServiceAssignsCaseAccess() {
        return when(assignCaseAccessService.applyDecision(isA(String.class), isA(CaseDetails.class)));
    }

    protected OngoingStubbing<String> whenServiceGetsSysUserToken() {
        return when(systemUserService.getSysUserToken());
    }

    private void doRequestSetUpContested() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper
            .readTree(new File(getClass().getResource(PATH + "change-of-representatives-before.json").toURI()));
    }

    private void setUpCaseDetails(String fileName) throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            callbackRequest = mapper.readValue(resourceAsStream, CallbackRequest.class);
            caseDetails = callbackRequest.getCaseDetails();
        }
    }

    @Test
    public void updateRepresentation() throws Exception {
        doRequestSetUpContested();
        setUpCaseDetails("change-of-representatives.json");
        whenServiceUpdatesRepresentation().thenReturn(caseDetails.getData());
        whenServiceAssignsCaseAccess().thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build());
        whenServiceGetsSysUserToken().thenReturn(AUTH_TOKEN);

        mvc.perform(post(updateEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ChangeOfRepresentatives[0].value.party", is("Applicant")))
            .andExpect(jsonPath("$.data.ChangeOfRepresentatives[0].value.name", is("John Smith")))
            .andExpect(jsonPath("$.data.ChangeOfRepresentatives[0].value.by", is("Claire Mumford")))
            .andExpect(jsonPath("$.data.ChangeOfRepresentatives[0].value.added.name", is("Sir Solicitor")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }

    @Test
    public void updateRepresentationError400() throws Exception {
        doRequestSetUpContested();
        mvc.perform(post(updateEndpoint())
                .content("kwuilebge")
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void updateRepresentationHttpError500() throws Exception {
        doRequestSetUpContested();
        whenServiceUpdatesRepresentation().thenThrow(feignError());

        mvc.perform(post(updateEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }
}
