package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDAfterSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentConfirmationService;

import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class PBAPaymentConfirmationController implements BaseController {
    private final PBAPaymentConfirmationService pbaPaymentConfirmationService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/pba-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<CCDAfterSubmitCallbackResponse> pbaConfirmation(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) throws IOException {
        log.info("Received request for PBA confirmation. Auth token: {}, Case request : {}", authToken, ccdRequest);

        validateCaseData(ccdRequest);

        String confirmationBody = pbaPaymentConfirmationService.paymentConfirmationMarkdown();
        log.info("confirmationBody : {}", confirmationBody);

        CCDAfterSubmitCallbackResponse callbackResponse = CCDAfterSubmitCallbackResponse.builder()
                .confirmationBody(confirmationBody)
                .build();

        return ResponseEntity.ok(callbackResponse);
    }

}
