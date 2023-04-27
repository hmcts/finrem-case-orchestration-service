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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeRespondentSolicitorAddress;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.NoCSolicitorDetailsHelper.removeSolicitorAddress;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAMILY_MEDIATOR_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FAST_TRACK_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_PHONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
@RequiredArgsConstructor
public class UpdateContestedCaseController extends BaseController {

    private static final String DIVORCE_STAGE_REACHED = "divorceStageReached";
    private static final String DIVORCE_UPLOAD_EVIDENCE_2 = "divorceUploadEvidence2";
    private static final String DIVORCE_DECREE_ABSOLUTE_DATE = "divorceDecreeAbsoluteDate";
    private static final String DIVORCE_UPLOAD_PETITION = "divorceUploadPetition";
    private static final String DIVORCE_UPLOAD_EVIDENCE_1 = "divorceUploadEvidence1";
    private static final String DIVORCE_DECREE_NISI_DATE = "divorceDecreeNisiDate";

    private final OnlineFormDocumentService onlineFormDocumentService;
    private final CaseFlagsService caseFlagsService;

    @PostMapping(path = "/update-contested-case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles update Contested Case details and cleans up the data fields based on the options chosen for Contested Cases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateContestedCase(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update Contested case with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);

        Map<String, Object> caseData = caseDetails.getData();
        String typeOfApplication = Objects.toString(caseData.get(TYPE_OF_APPLICATION), TYPE_OF_APPLICATION_DEFAULT_TO);
        if (typeOfApplication.equals(TYPE_OF_APPLICATION_DEFAULT_TO)) {
            updateDivorceDetailsForContestedCase(caseData);
        }
        caseFlagsService.setCaseFlagInformation(caseDetails);

        updateDivorceDetailsForContestedCase(caseData);
        updateContestedRespondentDetails(caseData);
        updateContestedPeriodicPaymentOrder(caseData, typeOfApplication);
        if (typeOfApplication.equals(TYPE_OF_APPLICATION_DEFAULT_TO)) {
            updateContestedPropertyAdjustmentOrder(caseData);
        }
        updateContestedFastTrackProcedureDetail(caseData);
        updateContestedComplexityDetails(caseData);
        isApplicantsHomeCourt(caseData);
        updateContestedMiamDetails(caseData);
        cleanupAdditionalDocuments(caseData);

        CaseDocument document = onlineFormDocumentService.generateDraftContestedMiniFormA(authToken, ccdRequest.getCaseDetails());
        caseData.put(MINI_FORM_A, document);
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
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update contested case solicitor contact details with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(removeSolicitorAddress(caseDetails, true)).build());
    }

    private void cleanupAdditionalDocuments(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("promptForAnyDocument"), NO_VALUE)) {
            caseData.put("uploadAdditionalDocument", null);
        }
    }

    private void updateContestedFastTrackProcedureDetail(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(FAST_TRACK_DECISION), NO_VALUE)) {
            caseData.put("fastTrackDecisionReason", null);
        }
    }

    private void updateContestedComplexityDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("addToComplexityListOfCourts"), "falseNo")) {
            removeContestedComplexityDetails(caseData);
        } else {
            updateComplexityDetails(caseData);
        }
    }

    private void updateComplexityDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("otherReasonForComplexity"), NO_VALUE)) {
            caseData.put("otherReasonForComplexityText", null);
        }
    }

    private void removeContestedComplexityDetails(Map<String, Object> caseData) {
        caseData.put("estimatedAssetsChecklist", null);
        caseData.put("netValueOfHome", null);
        caseData.put("potentialAllegationChecklist", null);
        caseData.put("otherReasonForComplexity", null);
        caseData.put("otherReasonForComplexityText", null);
        caseData.put("detailPotentialAllegation", null);
    }

    private void isApplicantsHomeCourt(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("isApplicantsHomeCourt"), NO_VALUE)) {
            caseData.put("reasonForLocalCourt", null);
        }
    }

    private void updateContestedMiamDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(APPLICANT_ATTENDED_MIAM), YES_VALUE)) {
            removeAllMiamExceptionDetails(caseData);
            removeMiamCertificationDetailsForApplicantAttendedMiam(caseData);
        } else {
            removeMiamCertificationDetails(caseData);
            updateWhenClaimingExemptionMiam(caseData);
        }
    }

    private void updateWhenClaimingExemptionMiam(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(CLAIMING_EXEMPTION_MIAM), NO_VALUE)) {
            caseData.put(FAMILY_MEDIATOR_MIAM, null);
            removeMiamExceptionDetails(caseData);
        } else {
            updateClaimingExemptionMiamDetails(caseData);
        }
    }

    private void removeMiamCertificationDetailsForApplicantAttendedMiam(Map<String, Object> caseData) {
        caseData.put("soleTraderName1", null);
        caseData.put("familyMediatorServiceName1", null);
        caseData.put("mediatorRegistrationNumber1", null);
    }

    private void removeMiamCertificationDetails(Map<String, Object> caseData) {
        removeMiamCertificationDetailsForApplicantAttendedMiam(caseData);
        caseData.put("soleTraderName", null);
        caseData.put("familyMediatorServiceName", null);
        caseData.put("mediatorRegistrationNumber", null);
    }

    private void removeAllMiamExceptionDetails(Map<String, Object> caseData) {
        caseData.put(CLAIMING_EXEMPTION_MIAM, null);
        caseData.put(FAMILY_MEDIATOR_MIAM, null);
        removeMiamExceptionDetails(caseData);
    }

    private void updateClaimingExemptionMiamDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(FAMILY_MEDIATOR_MIAM), YES_VALUE)) {
            removeMiamExceptionDetails(caseData);
        } else {
            removeMiamCertificationDetails(caseData);
            updateMiamExceptionDetails(caseData);
        }
    }

    private void updateMiamExceptionDetails(Map<String, Object> caseData) {
        ArrayList miamExemptionsChecklist = (ArrayList) caseData.get("MIAMExemptionsChecklist");
        removeExemptionCheckLists(caseData, miamExemptionsChecklist,
            "other", "MIAMOtherGroundsChecklist");

        removeExemptionCheckLists(caseData, miamExemptionsChecklist,
            "domesticViolence", "MIAMDomesticViolenceChecklist");

        removeExemptionCheckLists(caseData, miamExemptionsChecklist,
            "urgency", "MIAMUrgencyReasonChecklist");

        removeExemptionCheckLists(caseData, miamExemptionsChecklist,
            "previousMIAMattendance", "MIAMPreviousAttendanceChecklist");
    }

    private void removeExemptionCheckLists(Map<String, Object> caseData, ArrayList miamExemptionsChecklist,
                                           String other, String miamOtherGroundsChecklist) {
        if (hasNotSelected(miamExemptionsChecklist, other)) {
            caseData.put(miamOtherGroundsChecklist, null);
        }
    }

    private void removeMiamExceptionDetails(Map<String, Object> caseData) {
        caseData.put("MIAMExemptionsChecklist", null);
        caseData.put("MIAMDomesticViolenceChecklist", null);
        caseData.put("MIAMUrgencyReasonChecklist", null);
        caseData.put("MIAMPreviousAttendanceChecklist", null);
        caseData.put("MIAMOtherGroundsChecklist", null);
    }

    private void updateContestedPeriodicPaymentOrder(Map<String, Object> caseData, String typeOfApplication) {
        ArrayList natureOfApplicationList = typeOfApplication.equals(TYPE_OF_APPLICATION_DEFAULT_TO)
            ? (ArrayList) caseData.get("natureOfApplicationChecklist") : (ArrayList) caseData.get("natureOfApplicationChecklistSchedule");
        if (hasNotSelected(natureOfApplicationList, "periodicalPaymentOrder")) {
            removeContestedPeriodicalPaymentOrderDetails(caseData, typeOfApplication);
        } else {
            updateContestedPeriodicPaymentDetails(caseData, typeOfApplication);
        }
    }

    private void updateContestedPeriodicPaymentDetails(Map<String, Object> caseData, String typeOfApplication) {
        String paymentForChildrenDecisionObj = Objects.toString(caseData.get("paymentForChildrenDecision"));

        if (equalsTo(paymentForChildrenDecisionObj, NO_VALUE)) {
            removeBenefitsDetails(caseData, typeOfApplication);
        } else {
            if (equalsTo(paymentForChildrenDecisionObj, YES_VALUE)) {
                removeBenefitPaymentChecklist(caseData, typeOfApplication);
            }
        }
    }

    private void removeBenefitPaymentChecklist(Map<String, Object> caseData, String typeOfApplication) {
        if (typeOfApplication.equals(TYPE_OF_APPLICATION_DEFAULT_TO)) {
            caseData.put("benefitPaymentChecklist", null);
        } else {
            caseData.put("benefitPaymentChecklistSchedule", null);
        }
    }

    private void removeBenefitsDetails(Map<String, Object> caseData, String typeOfApplication) {
        if (typeOfApplication.equals(TYPE_OF_APPLICATION_DEFAULT_TO)) {
            caseData.put("benefitForChildrenDecision", null);
            caseData.put("benefitPaymentChecklist", null);
        } else {
            caseData.put("benefitForChildrenDecisionSchedule", null);
            caseData.put("benefitPaymentChecklistSchedule", null);
        }
    }

    private void removeContestedPeriodicalPaymentOrderDetails(Map<String, Object> caseData, String typeOfApplication) {
        caseData.put("paymentForChildrenDecision", null);
        removeBenefitsDetails(caseData, typeOfApplication);
    }

    private void updateContestedPropertyAdjustmentOrder(Map<String, Object> caseData) {
        ArrayList natureOfApplicationList = (ArrayList) caseData.get("natureOfApplicationChecklist");
        if (hasNotSelected(natureOfApplicationList, "propertyAdjustmentOrder")) {
            removePropertyAdjustmentOrder(caseData);
        } else {
            updatePropertyAdjustmentOrderDetails(caseData);
        }
    }

    private void updatePropertyAdjustmentOrderDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("additionalPropertyOrderDecision"), NO_VALUE)) {
            caseData.put("propertyAdjutmentOrderDetail", null);
        }
    }

    private void removePropertyAdjustmentOrder(Map<String, Object> caseData) {
        caseData.put("propertyAddress", null);
        caseData.put("mortgageDetail", null);
        caseData.put("propertyAdjutmentOrderDetail", null);
    }

    private void updateDivorceDetailsForContestedCase(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(DIVORCE_STAGE_REACHED), "Decree Nisi")) {
            // remove Decree Absolute details
            caseData.put(DIVORCE_UPLOAD_EVIDENCE_2, null);
            caseData.put(DIVORCE_DECREE_ABSOLUTE_DATE, null);
            caseData.put(DIVORCE_UPLOAD_PETITION, null);
        } else if (equalsTo((String) caseData.get(DIVORCE_STAGE_REACHED), "Decree Absolute")) {
            // remove Decree Nisi details
            caseData.put(DIVORCE_UPLOAD_EVIDENCE_1, null);
            caseData.put(DIVORCE_DECREE_NISI_DATE, null);
            caseData.put(DIVORCE_UPLOAD_PETITION, null);
        } else {
            // remove Decree Nisi details
            caseData.put(DIVORCE_UPLOAD_EVIDENCE_1, null);
            caseData.put(DIVORCE_DECREE_NISI_DATE, null);
            // remove Decree Absolute date
            caseData.put(DIVORCE_UPLOAD_EVIDENCE_2, null);
            caseData.put(DIVORCE_DECREE_ABSOLUTE_DATE, null);
        }
    }

    private void updateContestedRespondentDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get(CONTESTED_RESPONDENT_REPRESENTED), NO_VALUE)) {
            removeRespondentSolicitorAddress(caseData);
        } else {
            removeContestedRespondentAddress(caseData);
        }
    }

    private void removeContestedRespondentAddress(Map<String, Object> caseData) {
        caseData.put(RESPONDENT_ADDRESS, null);
        caseData.put(RESPONDENT_PHONE, null);
        caseData.put(RESPONDENT_EMAIL, null);
    }

    private boolean equalsTo(String fieldData, String value) {
        return nonNull(fieldData) && value.equalsIgnoreCase(fieldData.trim());
    }

    private boolean hasNotSelected(List<String> list, String option) {
        return nonNull(list) && !list.contains(option);
    }
}
