package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import java.time.ZonedDateTime;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedStatus.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class CaseDataController implements BaseController {



    @PostMapping(path = "/application-submitted-to-issued", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> applicationSubmittedToIssuedState(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Current case state {}, Case request : {}", caseDetails.getState(), callbackRequest);
        Map<String, Object> caseData = caseDetails.getData();
        if (caseDetails.getState().equalsIgnoreCase(APPLICATION_SUBMITTED.toString())) {
            caseData.put(STATE,APPLICATION_ISSUED.toString());
            caseData.put(ISSUE_DATE, ZonedDateTime.now().toLocalDate());
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }


}
