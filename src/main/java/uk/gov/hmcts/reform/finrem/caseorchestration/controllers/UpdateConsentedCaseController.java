package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class UpdateConsentedCaseController implements BaseController {

    private static final String DIVORCE_STAGE_REACHED = "divorceStageReached";
    private static final String DIVORCE_UPLOAD_EVIDENCE_2 = "divorceUploadEvidence2";
    private static final String DIVORCE_DECREE_ABSOLUTE_DATE = "divorceDecreeAbsoluteDate";
    private static final String DIVORCE_UPLOAD_EVIDENCE_1 = "divorceUploadEvidence1";
    private static final String DIVORCE_DECREE_NISI_DATE = "divorceDecreeNisiDate";

    @Autowired
    private ConsentOrderService consentOrderService;

    @PostMapping(path = "/update-case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles update case details and cleans up the data fields based on the options chosen for Consented Cases")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> updateCase(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Received request to update consented case with Case ID: {}", caseDetails.getId());

        validateCaseData(ccdRequest);
        Map<String, Object> caseData = caseDetails.getData();

        updateDivorceDetails(caseData);
        updatePeriodicPaymentData(caseData);
        updatePropertyDetails(caseData);
        updateRespondentSolicitorAddress(caseData);
        updateD81Details(caseData);
        updateApplicantOrSolicitorContactDetails(caseData);
        updateLatestConsentOrder(ccdRequest);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void updateLatestConsentOrder(CallbackRequest callbackRequest) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        caseData.put(LATEST_CONSENT_ORDER, consentOrderService.getLatestConsentOrderData(callbackRequest));
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


    private void updateApplicantOrSolicitorContactDetails(Map<String, Object> caseData) {
        if (equalsTo((String) caseData.get("applicantRepresented"), "No")) {
            removeApplicantSolicitorAddress(caseData);
        } else {
            removeApplicantAddress(caseData);
        }
    }

    private void removeApplicantSolicitorAddress(Map<String, Object> caseData) {
        caseData.put("solicitorName", null);
        caseData.put("solicitorFirm", null);
        caseData.put("solicitorReference", null);
        caseData.put("solicitorAddress", null);
        caseData.put("solicitorPhone", null);
        caseData.put("solicitorEmail", null);
        caseData.put("solicitorDXnumber", null);
        caseData.put(SOLICITOR_AGREE_TO_RECEIVE_EMAILS, null);
    }

    private void removeApplicantAddress(Map<String, Object> caseData) {
        caseData.put("applicantAddress", null);
        caseData.put("applicantPhone", null);
        caseData.put("applicantEmail", null);
    }

    private boolean equalsTo(String fieldData, String value) {
        return nonNull(fieldData) && value.equalsIgnoreCase(fieldData.trim());
    }

    private boolean hasNotSelected(List<String> list, String option) {
        return nonNull(list) && !list.contains(option);
    }
}
