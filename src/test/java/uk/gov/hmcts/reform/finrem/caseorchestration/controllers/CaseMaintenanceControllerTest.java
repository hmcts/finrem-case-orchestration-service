package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import javax.ws.rs.core.MediaType;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.BINARY_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.DOC_URL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.FILE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.caseDocument;

@WebMvcTest(CaseMaintenanceController.class)
public class CaseMaintenanceControllerTest extends BaseControllerTest {

    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

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
    public void shouldDeleteDecreeNisiWhenSolicitorChooseToDecreeAbsolute() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/updatecase/amend-divorce-details-decree-nisi.json").toURI()));
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.divorceUploadEvidence2").doesNotExist())
                .andExpect(jsonPath("$.data.divorceDecreeAbsoluteDate").doesNotExist());
    }

    @Test
    public void shouldDeleteDecreeAbsoluteWhenSolicitorChooseToDecreeNisi() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/updatecase/amend-divorce-details-decree-absolute.json").toURI()));
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.divorceUploadEvidence1").doesNotExist())
                .andExpect(jsonPath("$.data.divorceDecreeNisiDate").doesNotExist());
    }

    @Test
    public void shouldDeleteD81IndividualData() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/updatecase/amend-divorce-details-d81-joint.json").toURI()));
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.natureOfApplication3a").doesNotExist())
                .andExpect(jsonPath("$.data.natureOfApplication3b").doesNotExist());
    }

    @Test
    public void shouldNotRemovePropertyDetails() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/updatecase/amend-property-adjustment-details.json").toURI()));
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.natureOfApplication3a").exists())
                .andExpect(jsonPath("$.data.natureOfApplication3b").exists());
    }

    @Test
    public void shouldDeletePeriodicPaymentDetailsWithOutWrittenAgreement() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/updatecase/amend-periodic-payment-order-without-agreement.json").toURI()));
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.natureOfApplication6").exists())
                .andExpect(jsonPath("$.data.natureOfApplication7").exists());
    }


    @Test
    public void shouldDeletePeriodicPaymentDetailsWithWrittenAgreementForChildren() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/updatecase/amend-periodic-payment-order.json").toURI()));
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.natureOfApplication6").doesNotExist())
                .andExpect(jsonPath("$.data.natureOfApplication7").doesNotExist());
    }

    @Test
    public void shouldDeletePeriodicPaymentDetailsIfUnchecked() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/updatecase/amend-remove-periodic-payment-order.json").toURI()));
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/updatecase/remove-respondant-solicitor-details.json").toURI()));
        mvc.perform(post("/case-orchestration/update-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
    public void shouldDeleteDecreeNisiWhenSolicitorChooseToDecreeAbsoluteForContested() throws Exception {
        when(onlineFormDocumentService.generateMiniFormA(eq(BEARER_TOKEN), isA(CaseDetails.class)))
                .thenReturn(caseDocument());
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/amend-divorce-details-decree-nisi.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.divorceUploadEvidence2").doesNotExist())
                .andExpect(jsonPath("$.data.divorceDecreeAbsoluteDate").doesNotExist())
                .andExpect(jsonPath("$.data.miniFormA.document_url", is(DOC_URL)))
                .andExpect(jsonPath("$.data.miniFormA.document_filename", is(FILE_NAME)))
                .andExpect(jsonPath("$.data.miniFormA.document_binary_url", is(BINARY_URL)));
    }

    @Test
    public void shouldDeleteDecreeAbsoluteWhenSolicitorChooseToDecreeNisiForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/amend-divorce-details-decree-absolute.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.divorceUploadEvidence1").doesNotExist())
                .andExpect(jsonPath("$.data.divorceDecreeNisiDate").doesNotExist());
    }

    @Test
    public void shouldRemovePropertyAdjustmentOrderDetailsWhenSolicitorUncheckedForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/remove-property-adjustment-order-details.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.propertyAdjutmentOrderDetail").doesNotExist());
    }

    @Test
    public void shouldRemovePeriodicPaymentOrderDetailsWhenSolicitorUncheckedForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/remove-periodic-payment-order-details.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.paymentForChildrenDecision").doesNotExist())
                .andExpect(jsonPath("$.data.benefitForChildrenDecision").doesNotExist())
                .andExpect(jsonPath("$.data.benefitPaymentChecklist").doesNotExist());
    }

    @Test
    public void shouldUpdatePeriodicPaymentDetailsWhenPaymentForChildrenIsUncheckedForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/"
                        + "update-periodic-payment-details-for-no-payment-for-children.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.benefitForChildrenDecision").doesNotExist())
                .andExpect(jsonPath("$.data.benefitPaymentChecklist").doesNotExist());
    }

    @Test
    public void shouldUpdatePeriodicPaymentDetailsWhenBenefitsForChildrenForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/"
                        + "update-periodic-payment-details-with-benefits-for-children.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.benefitPaymentChecklist").doesNotExist());
    }

    @Test
    public void shouldRemoveSolicitorDetailsWhenRespondentIsNotRepresentedBySolicitorForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/remove-respondent-solicitor-details.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.fastTrackDecisionReason").doesNotExist());
    }

    @Test
    public void shouldNotUpdateFastTrackDetailsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/remove-respondent-solicitor-details.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.fastTrackDecisionReason").exists());
    }

    @Test
    public void shouldRemoveComplexityDetailsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/remove-complexity-details.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.otherReasonForComplexityText").doesNotExist());
    }

    @Test
    public void shouldRemoveReasonForLocalCourtForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/is-applicant-home-court.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.reasonForLocalCourt").doesNotExist());
    }

    @Test
    public void shouldRemoveMiamExceptionsWhenApplicantAttendedMiamForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/contested/remove-exceptions-when-applicant-attended-miam.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "update-miam-exceptions-when-applicant-not-claiming-exemption.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "update-miam-exceptions-when-applicant-attended-family-mediator.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "remove-domestic-violence-checklist.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "remove-urgency-checklist.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "remove-previousMiamAttendance-checklist.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "remove-other-checklist.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "do-not-remove-checklists.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "remove-miam-certification-details.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
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
                .getResource("/fixtures/contested/"
                        + "cleanup-miam-certification-details-when-applicant-attended-miam.json").toURI()));
        mvc.perform(post("/case-orchestration/update-contested-case")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.soleTraderName").exists())
                .andExpect(jsonPath("$.data.soleTraderName1").doesNotExist())
                .andExpect(jsonPath("$.data.familyMediatorServiceName1").doesNotExist())
                .andExpect(jsonPath("$.data.familyMediatorServiceName").exists())
                .andExpect(jsonPath("$.data.mediatorRegistrationNumber").exists())
                .andExpect(jsonPath("$.data.mediatorRegistrationNumber1").doesNotExist());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource(jsonFixture()).toURI()));
    }

    private String jsonFixture() {
        return "/fixtures/fee-lookup.json";
    }
}