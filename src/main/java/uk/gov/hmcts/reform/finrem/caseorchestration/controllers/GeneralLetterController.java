package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import javax.validation.constraints.NotNull;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_ADDRESS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_BODY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_PREVIEW;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterController extends BaseController {

    private final IdamService idamService;
    private final GeneralLetterService generalLetterService;

    @PostMapping(path = "/general-letter-start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Clears previous entered field values. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> initialiseGeneralLetterProperties(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to clear general letter fields for Case ID: {}", caseDetails.getId());

        validateCaseData(callback);

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(GENERAL_LETTER_ADDRESS_TO, null);
        caseData.put("generalLetterRecipient", null);
        caseData.put("generalLetterRecipientAddress", null);
        caseData.put("generalLetterCreatedBy", idamService.getIdamFullName(authorisationToken));
        caseData.put(GENERAL_LETTER_BODY, null);
        caseData.remove(GENERAL_LETTER_PREVIEW);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/documents/preview-general-letter", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Preview general letter")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> previewGeneralLetter(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to preview general letter for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        if (generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails).isEmpty()) {
            generalLetterService.previewGeneralLetter(authorisationToken, caseDetails);
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
        } else {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails))
                .build());
        }
    }

    @PostMapping(path = "/documents/general-letter", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Creates general letter for case worker. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> createGeneralLetter(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for generating general letter with Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        if (generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails).isEmpty()) {
            generalLetterService.createGeneralLetter(authorisationToken, caseDetails);
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
        } else {
            return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(generalLetterService.getCaseDataErrorsForCreatingPreviewOrFinalLetter(caseDetails))
                .build());
        }
    }
}
