package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@WebMvcTest(UpdateContestedCaseController.class)
public class UpdateContestedCaseControllerTest extends BaseControllerTest {

    private static final String CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE = "/case-orchestration/update-contested-case";
    private static final String DATA_DIVORCE_UPLOAD_EVIDENCE_1 = "$.data.divorceUploadEvidence1";
    private static final String DATA_DIVORCE_DECREE_NISI_DATE = "$.data.divorceDecreeNisiDate";
    private static final String DIVORCE_PETITION_ISSUED_DATE = "$.data.divorcePetitionIssuedDate";
    private static final String DATA_DIVORCE_UPLOAD_PETITION = "$.data.divorceUploadPetition";
    private static final String DATA_DIVORCE_UPLOAD_EVIDENCE_2 = "$.data.divorceUploadEvidence2";
    private static final String DATA_DIVORCE_DECREE_ABSOLUTE_DATE = "$.data.divorceDecreeAbsoluteDate";

    private static final String FEE_LOOKUP_JSON = "/fixtures/fee-lookup.json";

    private ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode requestContent;

    @MockBean
    private OnlineFormDocumentService onlineFormDocumentService;

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
    public void shouldDeleteNDecreeAbsoluteWhenSolicitorChooseToDecreeNisiForContested() throws Exception {
        when(onlineFormDocumentService.generateDraftContestedMiniFormA(eq(AUTH_TOKEN), isA(CaseDetails.class)))
            .thenReturn(caseDocument());

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-divorce-details-decree-nisi.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath(DATA_DIVORCE_UPLOAD_EVIDENCE_2).doesNotExist())
            .andExpect(jsonPath(DATA_DIVORCE_DECREE_ABSOLUTE_DATE).doesNotExist())
            .andExpect(jsonPath(DIVORCE_PETITION_ISSUED_DATE).exists())
            .andExpect(jsonPath(DATA_DIVORCE_UPLOAD_PETITION).doesNotExist())
            .andExpect(jsonPath(DATA_DIVORCE_UPLOAD_EVIDENCE_1).exists())
            .andExpect(jsonPath(DATA_DIVORCE_DECREE_NISI_DATE).exists())
            .andExpect(jsonPath("$.data.miniFormA.document_url", is(DOC_URL)))
            .andExpect(jsonPath("$.data.miniFormA.document_filename", is(FILE_NAME)))
            .andExpect(jsonPath("$.data.miniFormA.document_binary_url", is(BINARY_URL)));
    }

    @Test
    public void shouldDeleteDecreeNisiWhenSolicitorChooseToDecreeAbsoluteForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-divorce-details-decree-absolute.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath(DIVORCE_PETITION_ISSUED_DATE).exists())
            .andExpect(jsonPath(DATA_DIVORCE_UPLOAD_PETITION).doesNotExist())
            .andExpect(jsonPath(DATA_DIVORCE_UPLOAD_EVIDENCE_1).doesNotExist())
            .andExpect(jsonPath(DATA_DIVORCE_DECREE_NISI_DATE).doesNotExist());
    }

    @Test
    public void shouldDeleteDecreeAbsoluteWhenSolicitorChooseToPetitionIssuedForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-divorce-details-petition-issued.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath(DATA_DIVORCE_UPLOAD_EVIDENCE_1).doesNotExist())
            .andExpect(jsonPath(DATA_DIVORCE_DECREE_NISI_DATE).doesNotExist())
            .andExpect(jsonPath(DIVORCE_PETITION_ISSUED_DATE).exists())
            .andExpect(jsonPath(DATA_DIVORCE_UPLOAD_PETITION).exists())
            .andExpect(jsonPath(DATA_DIVORCE_UPLOAD_EVIDENCE_2).doesNotExist())
            .andExpect(jsonPath(DATA_DIVORCE_DECREE_ABSOLUTE_DATE).doesNotExist());
    }

    @Test
    public void shouldRemovePropertyAdjustmentOrderDetailsWhenSolicitorUncheckedForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-property-adjustment-order-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.propertyAddress").doesNotExist())
            .andExpect(jsonPath("$.data.mortgageDetail").doesNotExist())
            .andExpect(jsonPath("$.data.propertyAdjutmentOrderDetail").doesNotExist());
    }

    @Test
    public void shouldUpdatePropertyAdjustmentOrderDecisionDetailForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-property-adjustment-order-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.propertyAddress").doesNotExist())
            .andExpect(jsonPath("$.data.mortgageDetail").doesNotExist())
            .andExpect(jsonPath("$.data.propertyAdjutmentOrderDetail").doesNotExist());
    }

    @Test
    public void shouldRemoveAdditionalPropertyDetailsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-additional-property-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.propertyAdjutmentOrderDetail").doesNotExist());
    }

    @Test
    public void shouldRemovePeriodicPaymentOrderDetailsWhenSolicitorUncheckedForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-periodic-payment-order-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.paymentForChildrenDecision").doesNotExist())
            .andExpect(jsonPath("$.data.benefitForChildrenDecision").doesNotExist())
            .andExpect(jsonPath("$.data.benefitPaymentChecklist").doesNotExist());
    }

    @Test
    public void shouldUpdatePeriodicPaymentDetailsWhenPaymentForChildrenIsUncheckedForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(
                "/fixtures/contested/update-periodic-payment-details-for-no-payment-for-children.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.benefitForChildrenDecision").doesNotExist())
            .andExpect(jsonPath("$.data.benefitPaymentChecklist").doesNotExist());
    }

    @Test
    public void shouldUpdatePeriodicPaymentDetailsWhenBenefitsForChildrenForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(
                "/fixtures/contested/update-periodic-payment-details-with-benefits-for-children.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.benefitPaymentChecklist").doesNotExist());
    }

    @Test
    public void shouldRemoveSolicitorDetailsWhenRespondentIsNotRepresentedBySolicitorForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-respondent-solicitor-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.rSolicitorName").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorFirm").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorReference").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorAddress").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorPhone").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorEmail").doesNotExist())
            .andExpect(jsonPath("$.data.rSolicitorDXnumber").doesNotExist());
    }

    @Test
    public void shouldRemoveRespondentAddressWhenRespondentIsRepresentedBySolicitorForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-respondent-address-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.respondentAddress").doesNotExist())
            .andExpect(jsonPath("$.data.respondentPhone").doesNotExist())
            .andExpect(jsonPath("$.data.respondentEmail").doesNotExist());
    }

    @Test
    public void shouldUpdateFastTrackDetailsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-respondent-address-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.fastTrackDecisionReason").doesNotExist());
    }

    @Test
    public void shouldNotUpdateFastTrackDetailsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-respondent-solicitor-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.fastTrackDecisionReason").exists());
    }

    @Test
    public void shouldRemoveComplexityDetailsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-complexity-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.estimatedAssetsChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.netValueOfHome").doesNotExist())
            .andExpect(jsonPath("$.data.potentialAllegationChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.otherReasonForComplexity").doesNotExist())
            .andExpect(jsonPath("$.data.otherReasonForComplexityText").doesNotExist())
            .andExpect(jsonPath("$.data.detailPotentialAllegation").doesNotExist());
    }

    @Test
    public void shouldRemoveAdditionalReasonForComplexityForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-other-reason-for-complexity.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.otherReasonForComplexityText").doesNotExist());
    }

    @Test
    public void shouldRemoveReasonForLocalCourtForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/is-applicant-home-court.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.reasonForLocalCourt").doesNotExist());
    }

    @Test
    public void shouldRemoveMiamExceptionsWhenApplicantAttendedMiamForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-exceptions-when-applicant-attended-miam.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.claimingExemptionMIAM").doesNotExist())
            .andExpect(jsonPath("$.data.familyMediatorMIAM").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMExemptionsChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMDomesticViolenceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMUrgencyReasonChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").doesNotExist());
    }

    @Test
    public void shouldUpdateMiamExceptionsWhenApplicantNotClaimingExceptionsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(
                "/fixtures/contested/update-miam-exceptions-when-applicant-not-claiming-exemption.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print()).andExpect(jsonPath("$.data.applicantAttendedMIAM").exists())
            .andExpect(jsonPath("$.data.claimingExemptionMIAM").exists())
            .andExpect(jsonPath("$.data.familyMediatorMIAM").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMExemptionsChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMDomesticViolenceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMUrgencyReasonChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").doesNotExist());
    }

    @Test
    public void shouldUpdateMiamExceptionsWhenApplicantHasFamilyMediatorForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(
                "/fixtures/contested/update-miam-exceptions-when-applicant-attended-family-mediator.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.MIAMExemptionsChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMDomesticViolenceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMUrgencyReasonChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").doesNotExist());
    }

    @Test
    public void shouldRemoveDomesticViolenceCheckListForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-domestic-violence-checklist.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.MIAMExemptionsChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMDomesticViolenceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMUrgencyReasonChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").exists());
    }

    @Test
    public void shouldRemoveUrgencyCheckListForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-urgency-checklist.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.MIAMExemptionsChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMDomesticViolenceChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMUrgencyReasonChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").exists());
    }

    @Test
    public void shouldRemovePreviousMiamAttendanceCheckListForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-previousMiamAttendance-checklist.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.MIAMExemptionsChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMDomesticViolenceChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMUrgencyReasonChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").exists());

    }

    @Test
    public void shouldRemoveOtherGroundsMiamCheckListForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-other-checklist.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.MIAMExemptionsChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMDomesticViolenceChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMUrgencyReasonChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").doesNotExist());
    }

    @Test
    public void shouldNotRemoveMiamCheckListForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/do-not-remove-checklists.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.MIAMExemptionsChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMDomesticViolenceChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMUrgencyReasonChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").exists())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").exists());
    }

    @Test
    public void shouldRemoveMiamCertificationDetailsWhenApplicantIsNotAttendedMiamForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-miam-certification-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.soleTraderName").doesNotExist())
            .andExpect(jsonPath("$.data.soleTraderName1").doesNotExist())
            .andExpect(jsonPath("$.data.familyMediatorServiceName1").doesNotExist())
            .andExpect(jsonPath("$.data.familyMediatorServiceName").doesNotExist())
            .andExpect(jsonPath("$.data.mediatorRegistrationNumber").doesNotExist())
            .andExpect(jsonPath("$.data.mediatorRegistrationNumber1").doesNotExist());
    }

    @Test
    public void shouldCleanupMiamCertificationWhenApplicantAttendedMiamForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(
                "/fixtures/contested/cleanup-miam-certification-details-when-applicant-attended-miam.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.soleTraderName").exists())
            .andExpect(jsonPath("$.data.soleTraderName1").doesNotExist())
            .andExpect(jsonPath("$.data.familyMediatorServiceName1").doesNotExist())
            .andExpect(jsonPath("$.data.familyMediatorServiceName").exists())
            .andExpect(jsonPath("$.data.mediatorRegistrationNumber").exists())
            .andExpect(jsonPath("$.data.mediatorRegistrationNumber1").doesNotExist());
    }

    @Test
    public void shouldCleanupAdditionalDocumentsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/cleanup-addtional-documents.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.promptForAnyDocument").exists())
            .andExpect(jsonPath("$.data.uploadAdditionalDocument").doesNotExist());
    }

    @Test
    public void shouldNotCleanupAdditionalDocumentsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-property-adjustment-order-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.promptForAnyDocument").exists())
            .andExpect(jsonPath("$.data.uploadAdditionalDocument").exists());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(FEE_LOOKUP_JSON).toURI()));
    }
}