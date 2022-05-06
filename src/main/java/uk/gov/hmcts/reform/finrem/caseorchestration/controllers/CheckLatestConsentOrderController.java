package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import javax.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@Slf4j
@RestController
@RequestMapping(value = "/case-orchestration")
public class CheckLatestConsentOrderController extends BaseController {

    @PostMapping(path = "/check-latest-consent-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Validation check for latest consent order field in CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateConsentOrderNotApproved(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        validateRequest(callback);

        Map<String, Object> caseData = callback.getCaseDetails().getData();
        long caseId = callback.getCaseDetails().getId();

        if (isNull(caseData.get(LATEST_CONSENT_ORDER))) {
            log.info("Failed validation for {} as latest Consent Order field was null", caseId);
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(Arrays.asList("Latest Consent Order Field is empty. Please use the Upload Consent Order Event instead of Send Order"))
                .build());
        }

        log.info("Successfully validated {} as latest Consent Order field was present", caseId);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().build());
    }
}