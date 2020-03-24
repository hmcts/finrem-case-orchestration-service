package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class ValidateHearingController implements BaseController {

    private final ValidateHearingService validateHearingService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/validate-hearing", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Validates a Hearing")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> validateHearing(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request for validating a hearing for Case ID: {}", callbackRequest.getCaseDetails().getId());

        validateCaseData(callbackRequest);

        List<String> errors = validateHearingService.validateHearingErrors(callbackRequest.getCaseDetails());
        if (!errors.isEmpty()) {
            log.info("Errors were found when validating case");
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build());
        }

        List<String> warnings = validateHearingService.validateHearingWarnings(callbackRequest.getCaseDetails());
        if (!warnings.isEmpty()) {
            log.info("Warnings were found when validating case");
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .warnings(warnings)
                .build());
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().build());
    }
}
