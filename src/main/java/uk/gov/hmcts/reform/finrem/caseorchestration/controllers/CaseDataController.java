package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdUpdateService;

import java.time.ZonedDateTime;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ISSUE_DATE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class CaseDataController implements BaseController {
    private final CcdUpdateService ccdUpdateService;

    @PostMapping(path = "/application-submitted-to-gate-keeping", consumes = APPLICATION_JSON, produces =
            APPLICATION_JSON)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> applicationSubmittedToGateKeepingState(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CallbackRequest callbackRequest) {
        validateCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Current case state {}, Case request : {}", caseDetails.getState(), callbackRequest);
        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(ISSUE_DATE, ZonedDateTime.now().toLocalDate());
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

}
