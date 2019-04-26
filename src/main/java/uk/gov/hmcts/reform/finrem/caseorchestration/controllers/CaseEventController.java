package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdUpdateService;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedEvent.ALLOCATE_TO_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ContestedEvent.ISSUE_APPLICATION;

@RestController
@RequestMapping("/case-orchestration")
public class CaseEventController implements BaseController {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @PostMapping(path = "/create-history-for-event", consumes = APPLICATION_JSON, produces =
            APPLICATION_JSON)
    public ResponseEntity<SubmittedCallbackResponse> createHistoryForEvent(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
        validateCaseData(callback);
        Map<String, Object> caseData = callback.getCaseDetails().getData();
        ccdUpdateService.createEvent(authToken, callback.getCaseDetails(), ISSUE_APPLICATION.getId(),
                ISSUE_APPLICATION.getEventSummary(), ISSUE_APPLICATION.getEventDescription());
        ccdUpdateService.createEvent(authToken, callback.getCaseDetails(), ALLOCATE_TO_JUDGE.getId(),
                ALLOCATE_TO_JUDGE.getEventSummary(), ALLOCATE_TO_JUDGE.getEventDescription());
        return ResponseEntity.ok(SubmittedCallbackResponse.builder().build());
    }
}
