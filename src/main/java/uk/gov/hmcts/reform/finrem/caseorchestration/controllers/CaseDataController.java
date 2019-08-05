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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.IS_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.YES;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class CaseDataController implements BaseController {
    private final IdamService idamService;

    @PostMapping(path = "/contested/set-defaults", consumes = APPLICATION_JSON, produces =
        APPLICATION_JSON)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> setDefaultValues(
        @RequestHeader(value = "Authorization", required = false) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Setting default values for contested journey.");
        validateCaseData(callbackRequest);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        setData(authToken, caseData);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void setData(String authToken, Map<String, Object> caseData) {
        if (idamService.isUserRoleAdmin(authToken)) {
            log.info("Admin users.");
            caseData.put(IS_ADMIN, YES);
        } else {
            log.info("other users.");
            caseData.put(IS_ADMIN, NO);
            caseData.put(APPLICANT_REPRESENTED, YES);
        }
    }
}
