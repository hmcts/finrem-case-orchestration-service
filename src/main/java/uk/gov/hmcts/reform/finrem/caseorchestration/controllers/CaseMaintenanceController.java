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
        updateRespondantSolicitorAddress(caseData);
        updateJointD81(caseData);
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    private void updateDivorceDetails(CaseData caseData) {
        if (caseData.getDivorceStageReached().equals("Decree Nisi")) {
            if (nonNull(caseData.getDivorceUploadEvidence2())) {
                caseData.setDivorceUploadEvidence2(null);
                caseData.setDivorceDecreeAbsoluteDate(null);
            }
        } else {
            if (nonNull(caseData.getDivorceUploadEvidence1())) {
                caseData.setDivorceUploadEvidence1(null);
                caseData.setDivorceDecreeNisiDate(null);
            }
        }

    }

    private void updatePeriodicPaymentData(CaseData caseData) {
        if (nonNull(caseData.getNatureOfApplication2())
                && !caseData.getNatureOfApplication2().contains("Periodical Payment Order")) {
            caseData.setNatureOfApplication5(null);
            caseData.setNatureOfApplication6(null);
            caseData.setNatureOfApplication7(null);
            caseData.setOrderForChildrenQuestion1(null);
        }
        if (nonNull(caseData.getNatureOfApplication2())
                && caseData.getNatureOfApplication2().contains("Periodical Payment Order")) {
            if (nonNull(caseData.getNatureOfApplication5())
                    && "Yes".equalsIgnoreCase(caseData.getNatureOfApplication5())) {
                caseData.setNatureOfApplication6(null);
                caseData.setNatureOfApplication7(null);
            }
        }

    }

    private void updatePropertyDetails(CaseData caseData) {
        if (nonNull(caseData.getNatureOfApplication2())
                && !caseData.getNatureOfApplication2().contains("Property Adjustment  Order")) {
            caseData.setNatureOfApplication3a(null);
            caseData.setNatureOfApplication3b(null);
        }
    }

    private void updateRespondantSolicitorAddress(CaseData caseData) {
        if (nonNull(caseData.getAppRespondentRep()) && "No".equalsIgnoreCase(caseData.getAppRespondentRep())) {
            caseData.setRespondentSolicitorName(null);
            caseData.setRespondentSolicitorFirm(null);
            caseData.setRespondentSolicitorReference(null);
            caseData.setRespondentSolicitorAddress(null);
            caseData.setRespondentSolicitorPhone(null);
            caseData.setRespondentSolicitorEmail(null);
            caseData.setRespondentSolicitorDxNumber(null);
        }
    }

    private void updateJointD81(CaseData caseData) {
        if (nonNull(caseData.getD81Question()) && "No".equalsIgnoreCase(caseData.getD81Question())) {
            caseData.setD81Joint(null);
        }

        if (nonNull(caseData.getD81Question()) && "Yes".equalsIgnoreCase(caseData.getD81Question())) {
            caseData.setD81Applicant(null);
            caseData.setD81Respondent(null);
        }
    }

}
