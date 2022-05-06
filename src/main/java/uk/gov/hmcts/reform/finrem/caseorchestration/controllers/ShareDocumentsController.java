package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ShareDocumentsService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_SHARE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_SHARE_DOCUMENTS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
public class ShareDocumentsController extends BaseController {

    private final ShareDocumentsService shareDocumentsService;

    @PostMapping(path = "/share-documents-with-respondent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Share documents with respondent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> shareDocumentsWithRespondent(
        @RequestBody CallbackRequest ccdRequest) {
        log.info("Received request to share documents with respondent");
        validateRequest(ccdRequest);

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        String applicantShareDocumentsValue = nullToEmpty(caseDetails.getData().get(APPLICANT_SHARE_DOCUMENTS));

        if (YES_VALUE.equalsIgnoreCase(applicantShareDocumentsValue)) {
            shareDocumentsService.shareDocumentsWithRespondent(caseDetails);
        } else if (NO_VALUE.equalsIgnoreCase(applicantShareDocumentsValue)) {
            shareDocumentsService.clearSharedDocumentsVisibleToRespondent(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }

    @PostMapping(path = "/share-documents-with-applicant", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Share documents with applicant")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> shareDocumentsWithApplicant(
        @RequestBody CallbackRequest ccdRequest) {
        log.info("Received request to share documents with applicant");
        validateRequest(ccdRequest);

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        String respondentShareDocumentsValue = nullToEmpty(caseDetails.getData().get(RESPONDENT_SHARE_DOCUMENTS));

        if (YES_VALUE.equalsIgnoreCase(respondentShareDocumentsValue)) {
            shareDocumentsService.shareDocumentsWithApplicant(caseDetails);
        } else if (NO_VALUE.equalsIgnoreCase(respondentShareDocumentsValue)) {
            shareDocumentsService.clearSharedDocumentsVisibleToApplicant(caseDetails);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }
}
