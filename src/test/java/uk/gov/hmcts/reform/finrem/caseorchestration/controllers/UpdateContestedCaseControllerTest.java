package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails.AmendApplicationDetailsAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamLegacyExemptionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

/**
 * @deprecated This controller will be removed in favour of using
 * {@link AmendApplicationDetailsAboutToSubmitHandler}.
 */
@WebMvcTest(UpdateContestedCaseController.class)
@ContextConfiguration(classes = {UpdateContestedCaseControllerTest.TestConfig.class})
@Import(MiamLegacyExemptionsService.class)
class UpdateContestedCaseControllerTest extends BaseControllerTest {

    private static final String CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE = "/case-orchestration/update-contested-case";
    private static final String DATA_DIVORCE_UPLOAD_EVIDENCE_1 = "$.data.divorceUploadEvidence1";
    private static final String DATA_DIVORCE_DECREE_NISI_DATE = "$.data.divorceDecreeNisiDate";
    private static final String DIVORCE_PETITION_ISSUED_DATE = "$.data.divorcePetitionIssuedDate";
    private static final String DATA_DIVORCE_UPLOAD_PETITION = "$.data.divorceUploadPetition";
    private static final String DATA_DIVORCE_UPLOAD_EVIDENCE_2 = "$.data.divorceUploadEvidence2";
    private static final String DATA_DIVORCE_DECREE_ABSOLUTE_DATE = "$.data.divorceDecreeAbsoluteDate";

    private static final String FEE_LOOKUP_JSON = "/fixtures/fee-lookup.json";

    private JsonNode requestContent;

    @MockitoBean
    private OnlineFormDocumentService onlineFormDocumentService;
    @MockitoBean
    private CaseFlagsService caseFlagsService;
    @MockitoBean
    private ExpressCaseService expressCaseService;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private MiamLegacyExemptionsService miamLegacyExemptionsService;

    @Configuration
    static class TestConfig {
        @Bean
        public FinremCaseDetailsMapper finremCaseDetailsMapper(ObjectMapper objectMapper) {
            return new FinremCaseDetailsMapper(objectMapper); // Provide a real FinremCaseDetailsMapper
        }
    }

    @BeforeEach
    public void setUp() {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void shouldDeleteNoDecreeAbsoluteWhenDecreeNisiSelectedBySolicitor() throws Exception {
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
    void shouldDeleteDecreeNisiWhenSolicitorChooseToDecreeAbsoluteForContested() throws Exception {
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
    void shouldDeleteDecreeAbsoluteWhenSolicitorChooseToPetitionIssuedForContested() throws Exception {
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
    void shouldRemovePropertyAdjustmentOrderDetailsWhenSolicitorUncheckedForContested() throws Exception {
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
    void shouldUpdatePropertyAdjustmentOrderDecisionDetailForContested() throws Exception {
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
    void shouldRemoveAdditionalPropertyDetailsForContested() throws Exception {
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
    void shouldRemovePeriodicPaymentOrderDetailsWhenSolicitorUncheckedForContested() throws Exception {
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
    void shouldRemovePeriodicPaymentOrderDetailsWhenSolicitorUncheckedForSchedule1Contested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-periodic-payment-order-details-schedule1.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.paymentForChildrenDecision").doesNotExist())
            .andExpect(jsonPath("$.data.benefitForChildrenDecisionSchedule").doesNotExist())
            .andExpect(jsonPath("$.data.benefitPaymentChecklistSchedule").doesNotExist());
    }

    @Test
    void shouldUpdatePeriodicPaymentDetailsWhenPaymentForChildrenIsUncheckedForContested() throws Exception {
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
    void shouldRemovePeriodicPaymentDetailsWithBenefitsForChildrenDecisionForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(
                // "benefitForChildrenDecision": "Yes",
                //  "paymentForChildrenDecision": "Yes",
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
    void shouldMaintainPeriodicPaymentDetailsWhenBenefitsForChildrenDecisionNotProvidedForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(
                // "paymentForChildrenDecision": "Yes",
                // "benefitForChildrenDecision": "No"
                "/fixtures/contested/update-periodic-payment-details-without-benefits-for-children-decision.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.benefitPaymentChecklist").exists());
    }

    @Test
    void shouldRemoveSolicitorDetailsWhenRespondentIsNotRepresentedBySolicitorForContested() throws Exception {
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
    void shouldRemoveRespondentAddressWhenRespondentIsRepresentedBySolicitorForContested() throws Exception {
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
    void shouldUpdateFastTrackDetailsForContested() throws Exception {
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
    void shouldNotUpdateFastTrackDetailsForContested() throws Exception {
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
    void shouldUpdateComplexityDetailsForContested() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/remove-complexity-details.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.estimatedAssetsChecklist").exists())
            .andExpect(jsonPath("$.data.netValueOfHome").exists())
            .andExpect(jsonPath("$.data.potentialAllegationChecklist").exists())
            .andExpect(jsonPath("$.data.otherReasonForComplexity").exists())
            .andExpect(jsonPath("$.data.otherReasonForComplexityText").exists())
            .andExpect(jsonPath("$.data.detailPotentialAllegation").exists())
            .andExpect(jsonPath("$.data.estimatedAssetsChecklistV2").exists());
    }

    @Test
    void shouldRemoveAdditionalReasonForComplexityForContested() throws Exception {
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
    void shouldRemoveReasonForLocalCourtForContested() throws Exception {
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
    void shouldRemoveMiamExceptionsWhenApplicantAttendedMiamForContested() throws Exception {
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
    void shouldOnlyRemoveLegacyMiamCheckListForContested() throws Exception {
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
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklistV2").exists())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklistV2").exists())
            .andExpect(jsonPath("$.data.MIAMPreviousAttendanceChecklist").doesNotExist())
            .andExpect(jsonPath("$.data.MIAMOtherGroundsChecklist").doesNotExist());
    }

    @Test
    void shouldRemoveMiamCertificationDetailsWhenApplicantIsNotAttendedMiamForContested() throws Exception {
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
    void shouldCleanupMiamCertificationWhenApplicantAttendedMiamForContested() throws Exception {
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
    void shouldCleanupAdditionalDocumentsForContested() throws Exception {
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
    void shouldNotCleanupAdditionalDocumentsForContested() throws Exception {
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

    @Test
    void shouldRemoveAllocatedToBeHeardAtHighCourtJudgeLevelText() throws Exception {
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/is-applicant-home-court.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.allocatedToBeHeardAtHighCourtJudgeLevelText").doesNotExist());
    }

    @Test
    void testPopulateInRefugeTabsCalled() throws Exception {

        // MockedStatic is closed after the try resources block
        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {

            requestContent = objectMapper.readTree(new File(getClass()
                    .getResource("/fixtures/contested/is-applicant-home-court.json").toURI()));
            mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
                            .content(requestContent.toString())
                            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.data.allocatedToBeHeardAtHighCourtJudgeLevelText").doesNotExist());

            // Check that methods to  is called by the controller
            mockedStatic.verify(() -> RefugeWrapperUtils.updateApplicantInRefugeTab(any()), times(1));
            mockedStatic.verify(() -> RefugeWrapperUtils.updateRespondentInRefugeTab(any()), times(1));
        }
    }

    @Test
    void testIsExpressPilotEnabledCalledOn() throws Exception {

        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-divorce-details-decree-nisi.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print());

        // Check that methods is called by the controller
        verify(expressCaseService, times(1)).setExpressCaseEnrollmentStatus(any());
    }

    @Test
    void testIsExpressPilotEnabledCalledOff() throws Exception {

        when(featureToggleService.isExpressPilotEnabled()).thenReturn(false);

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/contested/amend-divorce-details-decree-nisi.json").toURI()));
        mvc.perform(post(CASE_ORCHESTRATION_UPDATE_CONTESTED_CASE)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print());

        // Check that methods is not called by the controller
        verify(expressCaseService, never()).setExpressCaseEnrollmentStatus(any());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(FEE_LOOKUP_JSON).toURI()));
    }
}
