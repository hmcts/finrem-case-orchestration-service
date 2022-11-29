package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows.UpdateRepresentationWorkflowService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(UpdateConsentedCaseController.class)
public class UpdateConsentedCaseControllerTest extends BaseControllerTest {

    private static final String CASE_ORCHESTRATION_UPDATE_CASE = "/case-orchestration/update-case";
    private static final String UPDATE_CONTACT_DETAILS_ENDPOINT = "/case-orchestration/update-contact-details";
    private static final String CASE_ORCHESTRATION_UPDATE_CASE_SOLICITOR = "/case-orchestration/update-case-solicitor";
    private static final String FEE_LOOKUP_JSON = "/fixtures/fee-lookup.json";

    private ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode requestContent;

    @MockBean
    private ConsentOrderService consentOrderService;

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
    public void shouldDeleteDecreeNisiWhenSolicitorChooseToDecreeAbsolute() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-divorce-details-decree-nisi.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.divorceUploadEvidence2").doesNotExist())
            .andExpect(jsonPath("$.data.divorceDecreeAbsoluteDate").doesNotExist());
    }

    @Test
    public void shouldDeleteDecreeAbsoluteWhenSolicitorChooseToDecreeNisi() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-divorce-details-decree-absolute.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.divorceUploadEvidence1").doesNotExist())
            .andExpect(jsonPath("$.data.divorceDecreeNisiDate").doesNotExist());
    }

    @Test
    public void shouldDeleteD81IndividualData() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-divorce-details-d81-joint.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.d81Applicant").doesNotExist())
            .andExpect(jsonPath("$.data.d81Respondent").doesNotExist())
            .andExpect(jsonPath("$.data.d81Joint").exists());
    }

    @Test
    public void shouldDeleteD81JointData() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-divorce-details-d81-individual.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.d81Joint").doesNotExist())
            .andExpect(jsonPath("$.data.d81Applicant").exists())
            .andExpect(jsonPath("$.data.d81Respondent").exists());
    }

    @Test
    public void shouldDeletePropertyDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/remove-property-adjustment-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.natureOfApplication3a").doesNotExist())
            .andExpect(jsonPath("$.data.natureOfApplication3b").doesNotExist());
    }

    @Test
    public void shouldNotRemovePropertyDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-property-adjustment-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.natureOfApplication3a").exists())
            .andExpect(jsonPath("$.data.natureOfApplication3b").exists());
    }

    @Test
    public void shouldDeletePeriodicPaymentDetailsWithOutWrittenAgreement() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-periodic-payment-order-without-agreement.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.natureOfApplication6").exists())
            .andExpect(jsonPath("$.data.natureOfApplication7").exists());
    }


    @Test
    public void shouldDeletePeriodicPaymentDetailsWithWrittenAgreementForChildren() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-periodic-payment-order.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.natureOfApplication6").doesNotExist())
            .andExpect(jsonPath("$.data.natureOfApplication7").doesNotExist());
    }

    @Test
    public void shouldDeletePeriodicPaymentDetailsIfUnchecked() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-remove-periodic-payment-order.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.natureOfApplication5").doesNotExist())
            .andExpect(jsonPath("$.data.natureOfApplication6").doesNotExist())
            .andExpect(jsonPath("$.data.natureOfApplication7").doesNotExist())
            .andExpect(jsonPath("$.data.orderForChildrenQuestion1").doesNotExist());
    }

    @Test
    public void shouldDeleteRespondentSolicitorDetailsIfRespondentNotRepresentedBySolicitor() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/remove-respondent-solicitor-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.rSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorDXnumber").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorPhone").doesNotExist());
    }

    @Test
    public void shouldDeleteRespondentSolicitorContactDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/remove-respondent-solicitor-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE_SOLICITOR)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.rSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorPhone").doesNotExist())
            .andExpect(jsonPath("$.data.RespSolNotificationsEmailConsent").doesNotExist());
    }

    @Test
    public void shouldDeleteApplicantSolicitorContactDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/applicant-solicitor-to-draft-order-with-email-consent.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE_SOLICITOR)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.solicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorPhone").doesNotExist())
            .andExpect(jsonPath("$.data.solicitorAgreeToReceiveEmails").doesNotExist());
    }

    @Test
    public void shouldUpdateCaseDataWithLatestConsentOrder() throws Exception {
        when(consentOrderService.getLatestConsentOrderData(any(CallbackRequest.class))).thenReturn(getCaseDocument());
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/latestConsentedConsentOrder/"
                + "amend-consent-order-by-solicitor.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.latestConsentOrder").exists());
    }

    @Test
    public void shouldDeleteApplicantSolicitorDetailsIfApplicantNotRepresentedBySolicitor() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/remove-respondent-solicitor-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.rSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorDXnumber").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorPhone").doesNotExist());
    }

    @Test
    public void shouldDeleteApplicantSolicitorDetailsIfApplicantNotRepresentedIsNullBySolicitor() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/remove-respondent-solicitor-details.json").toURI()));

        assertNull(requestContent.get("case_details").get("case_data").get("applicantRepresented"));

        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.rSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorDXnumber").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorPhone").doesNotExist());
    }

    @Test
    public void shouldDeleteApplicantDetailsIfApplicantRepresentedIsYes() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/remove-respondent-solicitor-details.json").toURI()));

        assertNull(requestContent.get("case_details").get("case_data").get("applicantRepresented"));
        JsonNode removedNode = ((ObjectNode) requestContent.get("case_details").get("case_data"))
            .put("applicantRepresented", "Yes");
        assertEquals("Yes", requestContent.get("case_details").get("case_data").get("applicantRepresented").asText());

        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CASE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.applicantAddress").doesNotExist())
            .andExpect(jsonPath("$.data.applicantEmail").doesNotExist())
            .andExpect(jsonPath("$.data.applicantPhone").doesNotExist());
    }

    @Test
    public void givenValidData_whenUpdateCaseDetails_thenShouldPreserveOrgPolicies() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/updatecase/amend-divorce-details-d81-joint.json").toURI()));
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);
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
        when(featureToggleService.isCaseworkerNoCEnabled()).thenReturn(true);
        mvc.perform(post(UPDATE_CONTACT_DETAILS_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(FEE_LOOKUP_JSON).toURI()));
    }
}




