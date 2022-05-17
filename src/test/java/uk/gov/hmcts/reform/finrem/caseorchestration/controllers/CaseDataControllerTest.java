package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

import java.io.InputStream;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
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

    @Autowired private CaseDataController caseDataController;

    @MockBean private UpdateSolicitorDetailsService updateSolicitorDetailsService;
    @MockBean private IdamService idamService;
    @MockBean private FeatureToggleService featureToggleService;
    @MockBean private CaseDataService caseDataService;

    protected CaseDetails caseDetails;

    private void setUpCaseDetails(String fileName) throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try (InputStream resourceAsStream =
                 getClass().getResourceAsStream(PATH + fileName)) {
            caseDetails = mapper.readValue(resourceAsStream, CallbackRequest.class).getCaseDetails();
        }
    }

    @Test
    public void shouldSuccessfullyMoveCollection() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);
        String source = "uploadHearingOrder";
        String destination = "uploadHearingOrderRO";

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post(String.format("/case-orchestration/move-collection/%s/to/%s", source, destination))
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(caseDataService, times(1)).moveCollection(any(), eq(source), eq(destination));
    }

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

        verify(caseDataService, times(1)).setFinancialRemediesCourtDetails(any());
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminConsentedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);

        loadRequestContentWith(CONTESTED_HWF_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminConsentedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnAsAdminContestedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(true);

        loadRequestContentWith(CONTESTED_HWF_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isAdmin", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullyReturnNotAsAdminContestedPaperCase() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-defaults")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isAdmin", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.fastTrackDecision", is(NO_VALUE)))
            .andExpect(jsonPath("$.data.paperApplication", is(YES_VALUE)))
            .andExpect(jsonPath("$.data.applicantRepresented", is(YES_VALUE)));
    }

    @Test
    public void shouldSuccessfullySetOrgPolicy() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-org-policy")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy.OrgPolicyCaseAssignedRole", is(APP_SOLICITOR_POLICY)));
    }

    public void shouldNotSetOrgPolicyIfInvalidCaseType() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(Boolean.FALSE);
        when(caseDataService.isContestedApplication(any())).thenReturn(false);
        when(caseDataService.isConsentedApplication(any())).thenReturn(false);

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
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();

        caseDataController.setContestedDefaultValues(AUTH_TOKEN, callbackRequest);

        verify(updateSolicitorDetailsService, times(1)).setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddressContested_toggledOff() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);

        caseDataController.setContestedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(updateSolicitorDetailsService);
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddressContested_notRepresented() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);

        caseDataController.setContestedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(updateSolicitorDetailsService);
    }

    @Test
    public void shouldSuccessfullyPopulateApplicantSolicitorAddressConsented() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);

        CallbackRequest callbackRequest = buildCallbackRequest();

        caseDataController.setConsentedDefaultValues(AUTH_TOKEN, callbackRequest);

        verify(updateSolicitorDetailsService, times(1)).setApplicantSolicitorOrganisationDetails(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddressConsented_toggledOff() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);

        caseDataController.setConsentedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(updateSolicitorDetailsService);
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddressConsented_notRepresented() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);

        caseDataController.setConsentedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        verifyNoInteractions(updateSolicitorDetailsService);
    }

    @Test
    public void shouldSuccessfullySetDefaultValue() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/default-values")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.civilPartnership", is(NO_VALUE)));
    }

    @Test
    public void shouldSuccessfullySetOrgPolicies() throws Exception {

        loadRequestContentWith(PATH + "no-org-policies.json");
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);
        when(featureToggleService.isSolicitorNoticeOfChangeEnabled()).thenReturn(true);
        mvc.perform(post("/case-orchestration/org-policies")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy.OrgPolicyCaseAssignedRole",
                is(APP_SOLICITOR_POLICY)))
            .andExpect(jsonPath("$.data.RespondentOrganisationPolicy.OrgPolicyCaseAssignedRole",
                is(RESP_SOLICITOR_POLICY)))
            .andExpect(jsonPath("$.data.changeOrganisationRequestField").exists());
    }

    @Test
    public void shouldNotSetOrgPolicies() throws Exception {
        loadRequestContentWith(PATH + "no-orgs-is-represented.json");
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(featureToggleService.isSolicitorNoticeOfChangeEnabled()).thenReturn(true);
        mvc.perform(post("/case-orchestration/org-policies")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy").doesNotExist())
            .andExpect(jsonPath("$.data.RespondentOrganisationPolicy").doesNotExist())
            .andExpect(jsonPath("$.data.changeOrganisationRequestField").exists());
    }

    @Test
    public void shouldNotSetChangeRequestFieldWhenFeatureToggleDisabled() throws Exception {
        loadRequestContentWith(PATH + "no-orgs-is-represented.json");
        when(caseDataService.isRespondentRepresentedByASolicitor(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(featureToggleService.isSolicitorNoticeOfChangeEnabled()).thenReturn(false);
        mvc.perform(post("/case-orchestration/org-policies")
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.changeOrganisationRequestField").doesNotExist());
    }
}
