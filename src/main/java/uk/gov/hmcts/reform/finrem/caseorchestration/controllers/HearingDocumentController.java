package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isContestedPaperApplication;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class HearingDocumentController implements BaseController {

    private final HearingDocumentService hearingService;
    private final AdditionalHearingDocumentService additionalHearingService;
    private final ValidateHearingService validateHearingService;

    @PostMapping(path = "/documents/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Form C and G generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateHearingDocument(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) throws IOException {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for validating a hearing for Case ID: {}", caseDetails.getId());

        validateCaseData(callback);

        List<String> errors = validateHearingService.validateHearingErrors(caseDetails);
        if (!errors.isEmpty()) {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build());
        }

        Map<String, Object> caseData = caseDetails.getData();

        boolean hadFirstHearing = additionalHearingService.alreadyHadFirstHearing(caseDetails);

        if (!hadFirstHearing) {
            caseData.putAll(hearingService.generateHearingDocuments(authorisationToken, caseDetails));
        }

        if (isContestedPaperApplication(caseDetails)) {
            if (hadFirstHearing) {
                log.info("Sending Additional Hearing Document to bulk print for Contested Paper Case ID: {}", caseDetails.getId());
                additionalHearingService.createAndSendAdditionalHearingDocuments(authorisationToken, caseDetails);
            } else {
                log.info("Sending Forms A, C, G to bulk print for Contested Paper Case ID: {}", caseDetails.getId());
                hearingService.sendFormCAndGForBulkPrint(caseDetails, authorisationToken);
            }
        }

        List<String> warnings = validateHearingService.validateHearingWarnings(caseDetails);
        return ResponseEntity.ok(
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData).warnings(warnings).build());
    }

    @PostMapping(path = "/contested-upload-direction-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles direction order generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateHearingDocumentDirectionOrder(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {
        CaseDetails caseDetails = callback.getCaseDetails();
        validateCaseData(callback);
        Map<String, Object> caseData = caseDetails.getData();
        List<String> errors = new ArrayList<>();

        log.info("Storing Additional Hearing Document for Case ID: {}", caseDetails.getId());
        try {
            additionalHearingService.createAndStoreAdditionalHearingDocuments(authorisationToken, caseDetails);
        } catch (CourtDetailsParseException | JsonProcessingException e) {
            log.error(e.getMessage());
            errors.add(e.getMessage());
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .errors(errors)
            .build());
    }
}
