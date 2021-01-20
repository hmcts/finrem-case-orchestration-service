package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@SpringBootTest
@AutoConfigureMockMvc
public class CaseDataControllerTest extends BaseControllerTest {

    private static final String MOVE_VALUES_SAMPLE_JSON = "/fixtures/move-values-sample.json";
    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON = "/fixtures/contested/validate-hearing-successfully.json";
    private static final String INVALID_CASE_TYPE_JSON = "/fixtures/invalid-case-type.json";

    @MockBean private CaseDataService caseDataService;
    @MockBean private IdamService idamService;

    @Test
    public void shouldSuccessfullyMoveCollection() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(MOVE_VALUES_SAMPLE_JSON)
                .toURI()));
        mvc.perform(post("/case-orchestration/move-collection/uploadHearingOrder/to/uploadHearingOrderRO")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminConsented() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(CONTESTED_HWF_JSON).toURI()));
        mvc.perform(post("/case-orchestration/consented/set-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminConsented() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/consented/set-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminContested() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_HWF_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminContested() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminConsentedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(
            new File(getClass()
                .getResource(CONTESTED_HWF_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminConsentedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminContestedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.TRUE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_HWF_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminContestedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullySetOrgPolicy() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-org-policy")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy.OrgPolicyCaseAssignedRole", is(APP_SOLICITOR_POLICY)));
    }

    @Test
    public void shouldNotSetOrgPolicyIfInvalidCaseType() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);
        when(caseDataService.isContestedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(INVALID_CASE_TYPE_JSON).toURI()));
        mvc.perform(post("/case-orchestration/contested/set-paper-case-org-policy")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy").doesNotExist());
    }
}
