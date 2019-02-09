package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class PBAValidateController extends AbstractBaseController {

    private final PBAValidationService pbaValidationService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/pba-validate", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> pbaValidate(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) {
        log.info("Received request for PBA validate. Auth token: {}, Case request : {}", authToken, ccdRequest);

        validateCaseData(ccdRequest);

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (isPBAPayment(caseData)) {
            log.info("Validate PBA Number :  {}", caseData.getPbaNumber());
            if (!pbaValidationService.isValidPBA(authToken, caseData.getPbaNumber())) {
                log.info("PBA number is invalid.");
                return ResponseEntity.ok(CCDCallbackResponse.builder()
                        .errors(ImmutableList.of("PBA Account Number is not valid, please enter a valid one."))
                        .build());
            }
            log.info("PBA number is valid.");
        }
        return ResponseEntity.ok(CCDCallbackResponse.builder().build());
    }

}
