package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_NUMBER;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class PBAValidateController implements BaseController {

    private final PBAValidationService pbaValidationService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/pba-validate", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Validates if PBA Number provided is valid")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> pbaValidate(
            @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
            @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request for PBA validate. Auth token: {}, Case request : {}", authToken, callbackRequest);

        validateCaseData(callbackRequest);

        Map<String,Object> caseData = callbackRequest.getCaseDetails().getData();
        if (isPBAPayment(caseData)) {
            String pbaNumber = Objects.toString(caseData.get(PBA_NUMBER));
            log.info("Validate PBA Number :  {}", pbaNumber);
            if (!pbaValidationService.isValidPBA(authToken, pbaNumber)) {
                log.info("PBA number is invalid.");
                return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(ImmutableList.of("PBA Account Number is not valid, please enter a valid one."))
                    .build());
            }
            log.info("PBA number is valid.");
        }
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().build());
    }
}
