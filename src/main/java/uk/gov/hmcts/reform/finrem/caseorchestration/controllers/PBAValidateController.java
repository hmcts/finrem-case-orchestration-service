package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HELP_WITH_FEES_QUESTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_NUMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class PBAValidateController extends BaseController {

    private final PBAValidationService pbaValidationService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/pba-validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Validates if PBA Number provided is valid")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> pbaValidate(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to validate PBA number for Case ID: {}", caseDetails.getId());

        validateCaseData(callbackRequest);

        Map<String, Object> caseData = caseDetails.getData();
        boolean helpWithFeeQuestion = Objects.toString(caseData.get(HELP_WITH_FEES_QUESTION)).equalsIgnoreCase("no");
        if (helpWithFeeQuestion) {
            String pbaNumber = Objects.toString(caseData.get(PBA_NUMBER));
            log.info("Validating PBA Number: {}", pbaNumber);
            if (!pbaValidationService.isValidPBA(authToken, pbaNumber)) {
                log.info("PBA number for is invalid for Case ID: {}", caseDetails.getId());
                return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(List.of("PBA Account Number is not valid, please enter a valid one."))
                    .build());
            }
            log.info("PBA number is valid.");
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().build());
    }
}
