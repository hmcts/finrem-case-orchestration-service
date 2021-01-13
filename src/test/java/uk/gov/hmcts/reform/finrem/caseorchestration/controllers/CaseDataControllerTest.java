package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UpdateSolicitorDetailsService;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_ADDRESS;

@SpringBootTest
@AutoConfigureMockMvc
public class CaseDataControllerTest extends BaseControllerTest {

    private static final String CONTESTED_HWF_JSON = "/fixtures/contested/hwf.json";
    private static final String CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON = "/fixtures/contested/validate-hearing-successfully.json";

    @Autowired private CaseDataController caseDataController;

    @MockBean private UpdateSolicitorDetailsService updateSolicitorDetailsService;
    @MockBean private IdamService idamService;
    @MockBean private FeatureToggleService featureToggleService;
    @MockBean private CaseDataService caseDataService;

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
    public void shouldSuccessfullySetOrgPolicyIfToggleEnabled() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(featureToggleService.isShareACaseEnabled()).thenReturn(true);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-org-policy")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy.OrgPolicyCaseAssignedRole", is(APP_SOLICITOR_POLICY)));
    }

    @Test
    public void shouldNotSetOrgPolicyIfFeatureDisabled() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(featureToggleService.isShareACaseEnabled()).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);

        loadRequestContentWith(CONTESTED_VALIDATE_HEARING_SUCCESSFULLY_JSON);
        mvc.perform(post("/case-orchestration/contested/set-paper-case-org-policy")
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.ApplicantOrganisationPolicy").doesNotExist());
    }

    @Test
    public void shouldNotSetOrgPolicyIfFeatureEnabledButInvalidCaseType() throws Exception {
        when(idamService.isUserRoleAdmin(isA(String.class))).thenReturn(false);
        when(featureToggleService.isShareACaseEnabled()).thenReturn(true);
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
    public void shouldSuccessfullyPopulateApplicantSolicitorAddress() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(updateSolicitorDetailsService.getApplicantSolicitorAddressFromPrd(
            isA(String.class))).thenReturn(mockFirmAddress());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            caseDataController.setContestedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        Assert.assertEquals(response.getBody().getData().get(CONTESTED_SOLICITOR_ADDRESS), mockFirmAddress());

        verify(updateSolicitorDetailsService, times(1)).getApplicantSolicitorAddressFromPrd(AUTH_TOKEN);
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddress_toggledOff() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(false);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(updateSolicitorDetailsService.getApplicantSolicitorAddressFromPrd(
            isA(String.class))).thenReturn(mockFirmAddress());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            caseDataController.setContestedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        Assert.assertFalse(response.getBody().getData().containsKey(CONTESTED_SOLICITOR_ADDRESS));

        verify(updateSolicitorDetailsService, never()).getApplicantSolicitorAddressFromPrd(AUTH_TOKEN);
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddress_consentedApp() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isContestedApplication(any())).thenReturn(false);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(true);
        when(updateSolicitorDetailsService.getApplicantSolicitorAddressFromPrd(
            isA(String.class))).thenReturn(mockFirmAddress());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            caseDataController.setContestedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        Assert.assertFalse(response.getBody().getData().containsKey(CONTESTED_SOLICITOR_ADDRESS));

        verify(updateSolicitorDetailsService, never()).getApplicantSolicitorAddressFromPrd(AUTH_TOKEN);
    }

    @Test
    public void shouldNotPopulateApplicantSolicitorAddress_notRepresented() {
        when(featureToggleService.isRespondentJourneyEnabled()).thenReturn(true);
        when(caseDataService.isContestedApplication(any())).thenReturn(true);
        when(caseDataService.isApplicantRepresentedByASolicitor(any())).thenReturn(false);
        when(updateSolicitorDetailsService.getApplicantSolicitorAddressFromPrd(
            isA(String.class))).thenReturn(mockFirmAddress());

        ResponseEntity<AboutToStartOrSubmitCallbackResponse> response =
            caseDataController.setContestedDefaultValues(AUTH_TOKEN, buildCallbackRequest());

        Assert.assertFalse(response.getBody().getData().containsKey(CONTESTED_SOLICITOR_ADDRESS));

        verify(updateSolicitorDetailsService, never()).getApplicantSolicitorAddressFromPrd(AUTH_TOKEN);
    }

    private Map<String, Object> mockFirmAddress() {
        return objectMapper.convertValue(Address.builder()
            .addressLine1("Applicant Solicitor Firm")
            .addressLine2("AddressLine2")
            .addressLine3("AddressLine3")
            .county("County")
            .country("Country")
            .postTown("Town")
            .postCode("Postcode")
            .build(), Map.class);
    }
}
