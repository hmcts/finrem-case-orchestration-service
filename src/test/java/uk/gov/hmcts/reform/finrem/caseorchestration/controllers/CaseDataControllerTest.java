package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

import java.io.InputStream;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@SpringBootTest
@AutoConfigureMockMvc
public class CaseDataControllerTest extends BaseControllerTest {

    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String PATH = "/fixtures/noticeOfChange/";
    private static final String CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON = "/fixtures/contested/validate-hearing-successfully.json";

    @Autowired
    private CaseDataController caseDataController;

    @MockBean
    private UpdateSolicitorDetailsService updateSolicitorDetailsService;
    @MockBean
    private IdamService idamService;
    @MockBean
    private CaseDataService caseDataService;

    protected CaseDetails caseDetails;
    private CallbackRequest request;

    @Test
    public void shouldSuccessfullyReturnAsAdminConsented() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);

        loadRequestContentWith(CONTESTED_HWF_JSON);
        mvc.perform(post("/case-orchestration/consented/set-defaults")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminConsented() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/consented/set-defaults")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminContested() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);

        loadRequestContentWith(CONTESTED_HWF_JSON);
        mvc.perform(post("/case-orchestration/contested/set-defaults")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminContested() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/contested/set-defaults")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldPopulateFinancialRemediesCourtDetails() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post("/case-orchestration/contested/set-frc-details")
                .content(requestContent.toString())
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(caseDataService).setFinancialRemediesCourtDetails(any());
        verify(idamService, never()).isUserRoleAdmin(anyString());
    }

    @Test
    public void givenContestedCase_whenSolicitorTryToCreateCaseWithNonEligibleCourtSelected_thenShowError() throws Exception {
        doValidCourtDataSetUp();
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        mvc.perform(post("/case-orchestration/contested/set-frc-details")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors",
                hasItem(endsWith("You cannot select High Court or Royal Court of Justice. Please select another court."))));

        verify(caseDataService).setFinancialRemediesCourtDetails(any());
    }

    @Test
    public void givenContestedCase_whenCaseWorkerTryToCreateCase_thenDoNotShowError() throws Exception {
        doValidCourtDataSetUp();
        when(idamService.isUserRoleAdmin(anyString())).thenReturn(true);
        mvc.perform(post("/case-orchestration/contested/set-frc-details")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isEmpty());

        verify(caseDataService).setFinancialRemediesCourtDetails(any());
    }

    @Test
    public void shouldSuccessfullySetOrgPolicy() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(caseDataService.isContestedApplication(any(CaseDetails.class))).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-org-policy")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy.OrgPolicyCaseAssignedRole", is(APP_SOLICITOR_POLICY)))
            .andExpect(jsonPath("$.data.civilPartnership", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.promptForUrgentCaseQuestion", is(NO_VALUE)));
    }

    public void shouldNotSetOrgPolicyIfInvalidCaseType() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);
        when(caseDataService.isContestedApplication(any(CaseDetails.class))).thenReturn(false);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-org-policy")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy").doesNotExist());
    }

    @Test
    public void shouldSuccessfullyPopulateApplicantSolicitorAddressContested() {
        when(caseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();

        caseDataController.setContestedDefaultValues(AUTH_TOKEN, callbackRequest);

        verify(updateSolicitorDetailsService, times(1)).setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddressContested_notRepresented() {
        when(caseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(false);

        caseDataController.setContestedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(updateSolicitorDetailsService);
    }

    @Test
    public void shouldSuccessfullyPopulateApplicantSolicitorAddressConsented() {
        when(caseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();

        caseDataController.setConsentedDefaultValues(AUTH_TOKEN, callbackRequest);

        verify(updateSolicitorDetailsService, times(1)).setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddressConsented_notRepresented() {
        when(caseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(false);

        caseDataController.setConsentedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(updateSolicitorDetailsService);
    }

    public void createRequest(String payload) throws Exception {
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(payload)) {
            request = objectMapper.readValue(resourceAsStream, CallbackRequest.class);
            request.setCaseDetailsBefore(request.getCaseDetails());
        }
    }

    @Test
    public void shouldSuccessfullySetOrgPolicies() throws Exception {
        createRequest(PATH + "no-org-policies.json");
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(false);
        mvc.perform(post("/case-orchestration/org-policies")
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy.OrgPolicyCaseAssignedRole",
                is(APP_SOLICITOR_POLICY)))
            .andExpect(jsonPath("$.data.RespondentOrganisationPolicy.OrgPolicyCaseAssignedRole",
                is(RESP_SOLICITOR_POLICY)))
            .andExpect(jsonPath("$.data.changeOrganisationRequestField").exists())
            .andExpect(jsonPath("$.errors.errors").doesNotExist());
    }

    @Test
    public void shouldNotSetOrgPolicies() throws Exception {
        createRequest(PATH + "no-orgs-is-represented.json");
        when(caseDataService.isRespondentRepresentedByASolicitor(anyMap())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(anyMap())).thenReturn(true);
        mvc.perform(post("/case-orchestration/org-policies")
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy").doesNotExist())
            .andExpect(jsonPath("$.data.RespondentOrganisationPolicy").doesNotExist())
            .andExpect(jsonPath("$.data.changeOrganisationRequestField").exists());
    }
}
