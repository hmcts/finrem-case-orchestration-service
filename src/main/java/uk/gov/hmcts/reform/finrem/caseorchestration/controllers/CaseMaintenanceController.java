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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;

import java.util.List;

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
                    response = CCDCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> updateCase(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("Received request for updateCase ");
        validateCaseData(ccdRequest);
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        updateDivorceDetails(caseData);
        updatePeriodicPaymentData(caseData);
        updatePropertyDetails(caseData);
        updateRespondentSolicitorAddress(caseData);
        updateD81Details(caseData);
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    private void updateDivorceDetails(CaseData caseData) {
        if (caseData.getDivorceStageReached().equals("Decree Nisi")) {
            // remove Decree Absolute details
            caseData.setDivorceUploadEvidence2(null);
            caseData.setDivorceDecreeAbsoluteDate(null);
        } else {
            // remove Decree Nisi details
            caseData.setDivorceUploadEvidence1(null);
            caseData.setDivorceDecreeNisiDate(null);
        }
    }

    private void updatePeriodicPaymentData(CaseData caseData) {
        if (hasNotSelected(caseData.getNatureOfApplication2(), "Periodical Payment Order")) {
            removePeriodicPaymentData(caseData);
        } else {
            // if written agreement for order for children
            if (equalsTo(caseData.getNatureOfApplication5(), "Yes")) {
                caseData.setNatureOfApplication6(null);
                caseData.setNatureOfApplication7(null);
            }
        }
    }

    private void updatePropertyDetails(CaseData caseData) {
        if (hasNotSelected(caseData.getNatureOfApplication2(), "Property Adjustment  Order")) {
            removePropertyAdjustmentDetails(caseData);
        }
    }

    private void updateD81Details(CaseData caseData) {
        if (equalsTo(caseData.getD81Question(), "Yes")) {
            caseData.setD81Applicant(null);
            caseData.setD81Respondent(null);
        } else {
            caseData.setD81Joint(null);
        }
    }

    private void updateRespondentSolicitorAddress(CaseData caseData) {
        if (equalsTo(caseData.getAppRespondentRep(), "No")) {
            removeRespondentSolicitorAddress(caseData);
        }
    }

    private void removePeriodicPaymentData(CaseData caseData) {
        caseData.setNatureOfApplication5(null);
        caseData.setNatureOfApplication6(null);
        caseData.setNatureOfApplication7(null);
        caseData.setOrderForChildrenQuestion1(null);
    }

    private void removeRespondentSolicitorAddress(CaseData caseData) {
        caseData.setRespondentSolicitorName(null);
        caseData.setRespondentSolicitorFirm(null);
        caseData.setRespondentSolicitorReference(null);
        caseData.setRespondentSolicitorAddress(null);
        caseData.setRespondentSolicitorPhone(null);
        caseData.setRespondentSolicitorEmail(null);
        caseData.setRespondentSolicitorDxNumber(null);
    }

    private void removePropertyAdjustmentDetails(CaseData caseData) {
        caseData.setNatureOfApplication3a(null);
        caseData.setNatureOfApplication3b(null);
    }

    private boolean equalsTo(String fieldData, String value) {
        return nonNull(fieldData) && value.equalsIgnoreCase(fieldData);
    }

    private boolean hasNotSelected(List<String> natureOfApplication2, String option) {
        return nonNull(natureOfApplication2)
                && !natureOfApplication2.contains(option);
    }

}
