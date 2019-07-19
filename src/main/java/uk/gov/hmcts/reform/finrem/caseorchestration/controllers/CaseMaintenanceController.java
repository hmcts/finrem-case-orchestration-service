package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class CaseMaintenanceController implements BaseController {

    private static final String DIVORCE_STAGE_REACHED = "divorceStageReached";
    private static final String DIVORCE_UPLOAD_EVIDENCE_2 = "divorceUploadEvidence2";
    private static final String DIVORCE_DECREE_ABSOLUTE_DATE = "divorceDecreeAbsoluteDate";
    private static final String DIVORCE_UPLOAD_PETITION = "divorceUploadPetition";
    private static final String DIVORCE_UPLOAD_EVIDENCE_1 = "divorceUploadEvidence1";
    private static final String DIVORCE_DECREE_NISI_DATE = "divorceDecreeNisiDate";

    @Autowired
    private OnlineFormDocumentService onlineFormDocumentService;

    @Autowired
    private ConsentOrderService consentOrderService;

    @PostMapping(path = "/update-case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handles update Case details and cleans up the data fields based on the options choosen")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateCase(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CallbackRequest ccdRequest) {

        log.info("Received request for updateCase ");
        validateCaseData(ccdRequest);
        Map<String, Object> caseData = ccdRequest.getCaseDetails().getData();
        updateDivorceDetails(caseData);
        updatePeriodicPaymentData(caseData);
        updatePropertyDetails(caseData);
        updateRespondentSolicitorAddress(caseData);
        updateD81Details(caseData);
        updateLatestConsentOrder(ccdRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void updateLatestConsentOrder(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("latestConsentOrder", consentOrderService.getLatestConsentOrderData(callbackRequest));
    }

    @PostMapping(path = "/update-contested-case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Handles update Contested Case details and cleans up the data fields"
            + " based on the options choosen")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateContestedCase(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CallbackRequest ccdRequest) {

        log.info("Received request for contested - updateCase ");
        validateCaseData(ccdRequest);
        Map<String, Object> caseData = ccdRequest.getCaseDetails().getData();
        updateDivorceDetailsForContestedCase(caseData);
        updateContestedRespondentDetails(caseData);
        updateContestedPeriodicPaymentOrder(caseData);
        updateContestedPropertyAdjustmentOrder(caseData);
        updateContestedFastTrackProcedureDetail(caseData);
        updateContestedComplexityDetails(caseData);
        isApplicantsHomeCourt(caseData);
        updateContestedMiamDetails(caseData);
        cleanupAdditionalDocuments(caseData);
        CaseDocument document = onlineFormDocumentService.generateDraftContestedMiniFormA(authToken,
                ccdRequest.getCaseDetails());
        caseData.put(MINI_FORM_A, document);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void cleanupAdditionalDocuments(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("promptForAnyDocument"), "No")) {
            caseData.put("uploadAdditionalDocument", null);
        }
    }

    private void updateContestedFastTrackProcedureDetail(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("fastTrackDecision"), "No")) {
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
        if (equalsTo((String) caseData.get("otherReasonForComplexity"), "No")) {
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
        if (equalsTo((String) caseData.get("isApplicantsHomeCourt"), "No")) {
            caseData.put("reasonForLocalCourt", null);
        }
    }

    private void updateContestedMiamDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("applicantAttendedMIAM"), "Yes")) {
            removeAllMiamExceptionDetails(caseData);
            removeMiamCertificationDetailsForApplicantAttendedMiam(caseData);
        } else {
            removeMiamCertificationDetails(caseData);
            updateWhenClaimingExemptionMiam(caseData);
        }
    }

    private void updateWhenClaimingExemptionMiam(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("claimingExemptionMIAM"), "No")) {
            caseData.put("familyMediatorMIAM", null);
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
        caseData.put("claimingExemptionMIAM", null);
        caseData.put("familyMediatorMIAM", null);
        removeMiamExceptionDetails(caseData);
    }

    private void updateClaimingExemptionMiamDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("familyMediatorMIAM"), "Yes")) {
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

    private void updateContestedPeriodicPaymentOrder(Map<String, Object> caseData) {
        ArrayList natureOfApplicationList = (ArrayList) caseData.get("natureOfApplicationChecklist");
        if (hasNotSelected(natureOfApplicationList, "periodicalPaymentOrder")) {
            removeContestedPeriodicalPaymentOrderDetails(caseData);
        } else {
            updateContestedPeriodicPaymentDetails(caseData);
        }
    }

    private void updateContestedPeriodicPaymentDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("paymentForChildrenDecision"), "No")) {
            removeBenefitsDetails(caseData);
        } else {
            if (equalsTo((String) caseData.get("benefitForChildrenDecision"), "Yes")) {
                caseData.put("benefitPaymentChecklist", null);
            }
        }
    }

    private void removeBenefitsDetails(Map<String, Object> caseData) {
        caseData.put("benefitForChildrenDecision", null);
        caseData.put("benefitPaymentChecklist", null);
    }

    private void removeContestedPeriodicalPaymentOrderDetails(Map<String, Object> caseData) {
        caseData.put("paymentForChildrenDecision", null);
        removeBenefitsDetails(caseData);
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
        if (equalsTo((String) caseData.get("additionalPropertyOrderDecision"), "No")) {
            caseData.put("propertyAdjutmentOrderDetail", null);
        }
    }

    private void removePropertyAdjustmentOrder(Map<String, Object> caseData) {
        caseData.put("propertyAddress", null);
        caseData.put("mortgageDetail", null);
        caseData.put("propertyAdjutmentOrderDetail", null);
    }


    private void updateDivorceDetails(Map<String, Object> caseData) {
        if (caseData.get(DIVORCE_STAGE_REACHED).equals("Decree Nisi")) {
            // remove Decree Absolute details
            caseData.put(DIVORCE_UPLOAD_EVIDENCE_2, null);
            caseData.put(DIVORCE_DECREE_ABSOLUTE_DATE, null);
        } else {
            // remove Decree Nisi details
            caseData.put(DIVORCE_UPLOAD_EVIDENCE_1, null);
            caseData.put(DIVORCE_DECREE_NISI_DATE, null);
        }
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

    private void updatePeriodicPaymentData(Map<String, Object> caseData) {
        List natureOfApplication2 = (List) caseData.get("natureOfApplication2");

        if (hasNotSelected(natureOfApplication2, "Periodical Payment Order")) {
            removePeriodicPaymentData(caseData);
        } else {
            // if written agreement for order for children
            if (equalsTo((String) caseData.get("natureOfApplication5"), "Yes")) {
                caseData.put("natureOfApplication6", null);
                caseData.put("natureOfApplication7", null);
            }
        }
    }

    private void updatePropertyDetails(Map<String, Object> caseData) {
        List natureOfApplication2 = (List) caseData.get("natureOfApplication2");

        if (hasNotSelected(natureOfApplication2, "Property Adjustment  Order")) {
            removePropertyAdjustmentDetails(caseData);
        }
    }

    private void updateD81Details(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("d81Question"), "Yes")) {
            caseData.put("d81Applicant", null);
            caseData.put("d81Respondent", null);
        } else {
            caseData.put("d81Joint", null);
        }
    }

    private void updateRespondentSolicitorAddress(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("appRespondentRep"), "No")) {
            removeRespondentSolicitorAddress(caseData);
        }
    }

    private void removePeriodicPaymentData(Map<String, Object> caseData) {
        caseData.put("natureOfApplication5", null);
        caseData.put("natureOfApplication6", null);
        caseData.put("natureOfApplication7", null);
        caseData.put("orderForChildrenQuestion1", null);
    }

    private void removeRespondentSolicitorAddress(Map<String, Object> caseData) {
        caseData.put("rSolicitorName", null);
        caseData.put("rSolicitorFirm", null);
        caseData.put("rSolicitorReference", null);
        caseData.put("rSolicitorAddress", null);
        caseData.put("rSolicitorPhone", null);
        caseData.put("rSolicitorEmail", null);
        caseData.put("rSolicitorDXnumber", null);
    }

    private void removePropertyAdjustmentDetails(Map<String, Object> caseData) {
        caseData.put("natureOfApplication3a", null);
        caseData.put("natureOfApplication3b", null);
    }

    private void updateContestedRespondentDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("respondentRepresented"), "No")) {
            removeRespondentSolicitorAddress(caseData);
        } else {
            removeContestedRespondentAddress(caseData);
        }
    }

    private void removeContestedRespondentAddress(Map<String, Object> caseData) {
        caseData.put("respondentAddress", null);
        caseData.put("respondentPhone", null);
        caseData.put("respondentEmail", null);
    }


    private boolean equalsTo(String fieldData, String value) {
        return nonNull(fieldData) && value.equalsIgnoreCase(fieldData.trim());
    }

    private boolean hasNotSelected(List<String> list, String option) {
        return nonNull(list) && !list.contains(option);
    }

}
