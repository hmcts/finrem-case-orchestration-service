package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.time.LocalDate;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class ValidateHearingController implements BaseController {

    private final PBAValidationService pbaValidationService;

    @Autowired
    private ValidateHearingService validateHearingService;

    public static boolean isDateInBetweenIncludingEndPoints(final LocalDate min, final LocalDate max,
                                                            final LocalDate date) {
        return !(date.isBefore(min) || date.isAfter(max));
    }

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/validate-hearing", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> validateHearing(
        @RequestHeader(value = "Authorization", required = false) String authToken,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request for validating a hearing. Auth token: {}, Case request : {}", authToken,
            callbackRequest);

        validateCaseData(callbackRequest);

        List<String> errors = validateHearingService.validateHearingErrors(callbackRequest.getCaseDetails());
        if (!errors.isEmpty()) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errors)
                    .build());
        }

        List<String> warnings = validateHearingService.validateHearingWarnings(callbackRequest.getCaseDetails());
        if (!warnings.isEmpty()) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                    .warnings(warnings)
                    .build());
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().build());
    }
}
