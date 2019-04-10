package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class CaseMaintenanceController implements BaseController {

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
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void updateDivorceDetails(Map<String, Object> caseData) {
        if (caseData.get("divorceStageReached").equals("Decree Nisi")) {
            // remove Decree Absolute details
            caseData.put("divorceUploadEvidence2",null);
            caseData.put("divorceDecreeAbsoluteDate", null);
        } else {
            // remove Decree Nisi details
            caseData.put("divorceUploadEvidence1", null);
            caseData.put("divorceDecreeNisiDate", null);
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
        caseData.put("respondentSolicitorName", null);
        caseData.put("respondentSolicitorFirm", null);
        caseData.put("respondentSolicitorReference", null);
        caseData.put("respondentSolicitorAddress", null);
        caseData.put("respondentSolicitorPhone", null);
        caseData.put("respondentSolicitorEmail", null);
        caseData.put("respondentSolicitorDxNumber", null);
    }

    private void removePropertyAdjustmentDetails(Map<String, Object> caseData) {
        caseData.put("natureOfApplication3a", null);
        caseData.put("natureOfApplication3b", null);
    }

    private boolean equalsTo(String fieldData, String value) {
        return nonNull(fieldData) && value.equalsIgnoreCase(fieldData);
    }

    private boolean hasNotSelected(List<String> natureOfApplication2, String option) {
        return nonNull(natureOfApplication2)
                && !natureOfApplication2.contains(option);
    }

}
