package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@WebMvcTest(CaseAssignedRoleController.class)
public class CaseAssignedRoleControllerTest extends BaseControllerTest {

    private static final String GET_USER_ROLES = "/case-orchestration/get-user-roles";
    private static final String RESOURCE = "/fixtures/applicant-solicitor-to-draft-order-with-email-consent.json";
    private static final String SOLICITOR_ROLE_KEY = "currentUserCaseRole";
    private static final String CASE_DETAILS_KEY = "case_details";
    private static final String OTHER_ROLE = "otherRole";

    private ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode requestContent;

    @MockBean
    private CaseAssignedRoleService service;

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
    public void shouldReturnAppSolicitorRole() throws Exception {

        Map<String, Object> userRoles = new HashMap<>();
        userRoles.put(SOLICITOR_ROLE_KEY, APP_SOLICITOR_POLICY);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/applicant-solicitor-to-draft-order-with-email-consent.json").toURI()));
        CaseDetails caseDetails = objectMapper.convertValue(requestContent.get(CASE_DETAILS_KEY), CaseDetails.class);
        when(service.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN)).thenReturn(userRoles);
        mvc.perform(get(GET_USER_ROLES)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentUserCaseRole").exists())
            .andExpect(jsonPath("$.data.currentUserCaseRole")
                .value(equalToIgnoringCase((String) caseDetails.getData().get(SOLICITOR_ROLE_KEY))));
    }

    @Test
    public void shouldReturnRoleThatDoesNotMatchAppOrRespSolicitor() throws Exception {

        Map<String, Object> userRoles = new HashMap<>();
        userRoles.put(SOLICITOR_ROLE_KEY, OTHER_ROLE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/applicant-solicitor-to-draft-order-with-email-consent.json").toURI()));
        CaseDetails caseDetails = objectMapper.convertValue(requestContent.get(CASE_DETAILS_KEY), CaseDetails.class);
        when(service.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN)).thenReturn(userRoles);
        mvc.perform(get(GET_USER_ROLES)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentUserCaseRole").exists())
            .andExpect(jsonPath("$.data.currentUserCaseRole")
                .value(Matchers.not(equalToIgnoringCase((String) caseDetails.getData().get(SOLICITOR_ROLE_KEY)))));
    }

    @Test
    public void shouldReturnRespSolicitorRole() throws Exception {

        Map<String, Object> userRoles = new HashMap<>();
        userRoles.put(SOLICITOR_ROLE_KEY, RESP_SOLICITOR_POLICY);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/remove-respondent-solicitor-details.json").toURI()));
        CaseDetails caseDetails = objectMapper.convertValue(requestContent.get(CASE_DETAILS_KEY), CaseDetails.class);
        when(service.setCaseAssignedUserRole(caseDetails, AUTH_TOKEN)).thenReturn(userRoles);
        mvc.perform(get(GET_USER_ROLES)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentUserCaseRole").exists())
            .andExpect(jsonPath("$.data.currentUserCaseRole")
                .value(equalToIgnoringCase((String) caseDetails.getData().get(SOLICITOR_ROLE_KEY))));
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(RESOURCE).toURI()));
    }
}