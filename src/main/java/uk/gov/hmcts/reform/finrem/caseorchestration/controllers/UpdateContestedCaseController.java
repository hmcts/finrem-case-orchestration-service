package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Complexity;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.MiamExemption;
import uk.gov.hmcts.reform.finrem.ccd.domain.NatureApplication;
import uk.gov.hmcts.reform.finrem.ccd.domain.StageReached;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeSolicitorAddress;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
@RequiredArgsConstructor
public class UpdateContestedCaseController extends BaseController {

    private final OnlineFormDocumentService onlineFormDocumentService;
    private final FinremCallbackRequestDeserializer finremCallbackRequestDeserializer;

    @PostMapping(path = "/update-contested-case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles update Contested Case details and cleans up the data fields based on the options chosen for Contested Cases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateContestedCase(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody String source) {

        CallbackRequest ccdRequest = finremCallbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update Contested case with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);

        FinremCaseData caseData = caseDetails.getCaseData();
        updateDivorceDetailsForContestedCase(caseData);
        updateContestedRespondentDetails(caseData);
        updateContestedPeriodicPaymentOrder(caseData);
        updateContestedPropertyAdjustmentOrder(caseData);
        updateContestedFastTrackProcedureDetail(caseData);
        updateContestedComplexityDetails(caseData);
        isApplicantsHomeCourt(caseData);
        updateContestedMiamDetails(caseData);
        cleanupAdditionalDocuments(caseData);

        Document miniFormA = onlineFormDocumentService.generateDraftContestedMiniFormA(authToken, ccdRequest.getCaseDetails());
        Optional.ofNullable(miniFormA).ifPresent(
            document -> log.info("Draft MiniForm A Generated: filename={}, url={}, binUrl={}",
                document.getFilename(), document.getUrl(), document.getBinaryUrl()));

        caseData.setMiniFormA(miniFormA);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/update-contested-case-solicitor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles update case details and cleans up the data fields based on the options chosen for Consented Cases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateContestedCaseSolicitor(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody String source) {

        CallbackRequest ccdRequest = finremCallbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update contested case solicitor contact details with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(removeSolicitorAddress(caseDetails)).build());
    }

    private void cleanupAdditionalDocuments(FinremCaseData caseData) {
        if (caseData.getPromptForAnyDocument().isNoOrNull()) {
            caseData.setUploadAdditionalDocument(null);
        }
    }

    private void updateContestedFastTrackProcedureDetail(FinremCaseData caseData) {
        if (caseData.getFastTrackDecision().isNoOrNull()) {
            caseData.setFastTrackDecisionReason(null);
        }
    }

    private void updateContestedComplexityDetails(FinremCaseData caseData) {
        if (Complexity.FALSE_NO.equals(caseData.getAddToComplexityListOfCourts())) {
            removeContestedComplexityDetails(caseData);
        } else {
            updateComplexityDetails(caseData);
        }
    }

    private void updateComplexityDetails(FinremCaseData caseData) {
        if (caseData.getOtherReasonForComplexity().isNoOrNull()) {
            caseData.setOtherReasonForComplexityText(null);
        }
    }

    private void removeContestedComplexityDetails(FinremCaseData caseData) {
        caseData.setEstimatedAssetsChecklist(null);
        caseData.setNetValueOfHome(null);
        caseData.setPotentialAllegationChecklist(null);
        caseData.setOtherReasonForComplexity(null);
        caseData.setOtherReasonForComplexityText(null);
        caseData.setDetailPotentialAllegation(null);
    }

    private void isApplicantsHomeCourt(FinremCaseData caseData) {
        if (caseData.getIsApplicantsHomeCourt().isNoOrNull()) {
            caseData.setReasonForLocalCourt(null);
        }
    }

    private void updateContestedMiamDetails(FinremCaseData caseData) {
        if (caseData.getMiamWrapper().getApplicantAttendedMiam().isYes()) {
            removeAllMiamExceptionDetails(caseData);
            removeMiamCertificationDetailsForApplicantAttendedMiam(caseData);
        } else {
            removeMiamCertificationDetails(caseData);
            updateWhenClaimingExemptionMiam(caseData);
        }
    }

    private void updateWhenClaimingExemptionMiam(FinremCaseData caseData) {
        if (caseData.getMiamWrapper().getClaimingExemptionMiam().isNoOrNull()) {
            caseData.getMiamWrapper().setFamilyMediatorMiam(null);
            removeMiamExceptionDetails(caseData);
        } else {
            updateClaimingExemptionMiamDetails(caseData);
        }
    }

    private void removeMiamCertificationDetailsForApplicantAttendedMiam(FinremCaseData caseData) {
        caseData.setSoleTraderName1(null);
        caseData.setFamilyMediatorServiceName1(null);
        caseData.setMediatorRegistrationNumber1(null);
    }

    private void removeMiamCertificationDetails(FinremCaseData caseData) {
        removeMiamCertificationDetailsForApplicantAttendedMiam(caseData);
        caseData.setSoleTraderName(null);
        caseData.setFamilyMediatorServiceName(null);
        caseData.setMediatorRegistrationNumber(null);
    }

    private void removeAllMiamExceptionDetails(FinremCaseData caseData) {
        caseData.getMiamWrapper().setClaimingExemptionMiam(null);
        caseData.getMiamWrapper().setFamilyMediatorMiam(null);
        removeMiamExceptionDetails(caseData);
    }

    private void updateClaimingExemptionMiamDetails(FinremCaseData caseData) {
        if (caseData.getMiamWrapper().getFamilyMediatorMiam().isYes()) {
            removeMiamExceptionDetails(caseData);
        } else {
            removeMiamCertificationDetails(caseData);
            updateMiamExceptionDetails(caseData);
        }
    }

    private void updateMiamExceptionDetails(FinremCaseData caseData) {
        List<MiamExemption> miamExemptionsChecklist = caseData.getMiamWrapper().getMiamExemptionsChecklist();

        if (!miamExemptionsChecklist.contains(MiamExemption.OTHER)) {
            caseData.getMiamWrapper().setMiamOtherGroundsChecklist(null);
        }

        if (!miamExemptionsChecklist.contains(MiamExemption.DOMESTIC_VIOLENCE)) {
            caseData.getMiamWrapper().setMiamDomesticViolenceChecklist(null);
        }

        if (!miamExemptionsChecklist.contains(MiamExemption.URGENCY)) {
            caseData.getMiamWrapper().setMiamUrgencyReasonChecklist(null);
        }

        if (!miamExemptionsChecklist.contains(MiamExemption.PREVIOUS_MIAM_ATTENDANCE)) {
            caseData.getMiamWrapper().setMiamPreviousAttendanceChecklist(null);
        }
    }

    private void removeMiamExceptionDetails(FinremCaseData caseData) {
        caseData.getMiamWrapper().setMiamExemptionsChecklist(null);
        caseData.getMiamWrapper().setMiamDomesticViolenceChecklist(null);
        caseData.getMiamWrapper().setMiamUrgencyReasonChecklist(null);
        caseData.getMiamWrapper().setMiamPreviousAttendanceChecklist(null);
        caseData.getMiamWrapper().setMiamOtherGroundsChecklist(null);
    }

    private void updateContestedPeriodicPaymentOrder(FinremCaseData caseData) {
        List<NatureApplication> natureApplicationList = caseData.getNatureApplicationWrapper().getNatureOfApplicationChecklist();
        if (!natureApplicationList.contains(NatureApplication.PERIODICAL_PAYMENT_ORDER)) {
            removeContestedPeriodicalPaymentOrderDetails(caseData);
        } else {
            updateContestedPeriodicPaymentDetails(caseData);
        }
    }

    private void updateContestedPeriodicPaymentDetails(FinremCaseData caseData) {
        if (!caseData.getPaymentForChildrenDecision().isYes()) {
            removeBenefitsDetails(caseData);
            return;
        }
        if (caseData.getBenefitForChildrenDecision().isYes()) {
            caseData.setBenefitPaymentChecklist(null);
        }
    }

    private void removeBenefitsDetails(FinremCaseData caseData) {
        caseData.setBenefitForChildrenDecision(null);
        caseData.setBenefitPaymentChecklist(null);
    }

    private void removeContestedPeriodicalPaymentOrderDetails(FinremCaseData caseData) {
        caseData.setPaymentForChildrenDecision(null);
        removeBenefitsDetails(caseData);
    }

    private void updateContestedPropertyAdjustmentOrder(FinremCaseData caseData) {
        List<NatureApplication> natureOfApplicationList = caseData.getNatureApplicationWrapper()
            .getNatureOfApplicationChecklist();
        if (!natureOfApplicationList.contains(NatureApplication.PROPERTY_ADJUSTMENT_ORDER)) {
            removePropertyAdjustmentOrder(caseData);
        } else {
            updatePropertyAdjustmentOrderDetails(caseData);
        }
    }

    private void updatePropertyAdjustmentOrderDetails(FinremCaseData caseData) {
        if (caseData.getAdditionalPropertyOrderDecision().isNoOrNull()) {
            caseData.setPropertyAdjustmentOrderDetail(null);
        }
    }

    private void removePropertyAdjustmentOrder(FinremCaseData caseData) {
        caseData.setPropertyAddress(null);
        caseData.setMortgageDetail(null);
        caseData.setPropertyAdjustmentOrderDetail(null);
    }

    private void updateDivorceDetailsForContestedCase(FinremCaseData caseData) {
        if (StageReached.DECREE_NISI.equals(caseData.getDivorceStageReached())) {
            // remove Decree Absolute details
            caseData.setDivorceUploadEvidence2(null);
            caseData.setDivorceDecreeAbsoluteDate(null);
            caseData.setDivorceUploadPetition(null);
        } else if (StageReached.DECREE_ABSOLUTE.equals(caseData.getDivorceStageReached())) {
            // remove Decree Nisi details
            caseData.setDivorceUploadEvidence1(null);
            caseData.setDivorceDecreeNisiDate(null);
            caseData.setDivorceUploadPetition(null);
        } else {
            // remove Decree Nisi details
            caseData.setDivorceUploadEvidence1(null);
            caseData.setDivorceDecreeNisiDate(null);
            // remove Decree Absolute date
            caseData.setDivorceUploadEvidence2(null);
            caseData.setDivorceDecreeAbsoluteDate(null);
        }
    }

    private void removeRespondentSolicitorAddress(FinremCaseData caseData) {
        caseData.getContactDetailsWrapper().setRespondentSolicitorName(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorFirm(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorAddress(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorPhone(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorEmail(null);
        caseData.getContactDetailsWrapper().setRespondentSolicitorDxNumber(null);
    }

    private void updateContestedRespondentDetails(FinremCaseData caseData) {
        if (caseData.getContactDetailsWrapper().getContestedRespondentRepresented().isNoOrNull()) {
            removeRespondentSolicitorAddress(caseData);
        } else {
            removeContestedRespondentAddress(caseData);
        }
    }

    private void removeContestedRespondentAddress(FinremCaseData caseData) {
        caseData.getContactDetailsWrapper().setRespondentAddress(null);
        caseData.getContactDetailsWrapper().setRespondentPhone(null);
        caseData.getContactDetailsWrapper().setRespondentEmail(null);
    }
}
