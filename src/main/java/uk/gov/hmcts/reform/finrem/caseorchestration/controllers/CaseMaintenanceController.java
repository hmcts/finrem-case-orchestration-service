package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;

import java.util.Objects;
import java.util.concurrent.CancellationException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class CaseMaintenanceController implements BaseController {

    @PutMapping(path = "/update-case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> updateCase(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("Received request for updateCase ");
        validateCaseData(ccdRequest);
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }

    private void updateDivorceDetails(CaseData caseData) {
        if(caseData.getDivorceStageReached().equals("Decree Nisi")) {
            if(Objects.nonNull(caseData.getDivorceUploadEvidence2())) {
                caseData.setDivorceUploadEvidence2(null);
                caseData.setDivorceDecreeAbsoluteDate(null);
            }
        } else  {
            if(Objects.nonNull(caseData.getDivorceUploadEvidence1())) {
                caseData.setDivorceUploadEvidence1(null);
                caseData.setDivorceDecreeNisiDate(null);
            }
        }

    }

}
