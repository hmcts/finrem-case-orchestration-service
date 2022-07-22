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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedDraftOrderNotApprovedService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import javax.validation.constraints.NotNull;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class ContestedDraftOrderNotApprovedController extends BaseController {

    private final ContestedDraftOrderNotApprovedService contestedNotApprovedService;
    private final BulkPrintService bulkPrintService;
    private final PaperNotificationService paperNotificationService;
    private final DocumentHelper documentHelper;
    private final IdamService idamService;
    private final FinremCallbackRequestDeserializer callbackRequestDeserializer;

    @PostMapping(path = "/contested-application-not-approved-start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Clears previous entered field values. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> initialiseApplicationNotApprovedProperties(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

        CallbackRequest callback = callbackRequestDeserializer.deserialize(source);
        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to clear contested application not approved fields for Case ID: {}", caseDetails.getId());

        validateCaseData(callback);

        FinremCaseData caseData = caseDetails.getCaseData();
        caseData.setRefusalOrderJudgeType(null);
        caseData.setRefusalOrderDate(null);
        caseData.setRefusalOrderPreviewDocument(null);
        caseData.setRefusalOrderJudgeName(idamService.getIdamFullName(authorisationToken));

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/documents/preview-refusal-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Preview application not approved document")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> previewApplicationNotApprovedDocument(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

        CallbackRequest callback = callbackRequestDeserializer.deserialize(source);
        validateCaseData(callback);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();
        log.info("Received request to preview application not approved document for Case ID: {}", caseDetails.getId());

        contestedNotApprovedService.createAndSetRefusalOrderPreviewDocument(authorisationToken, caseDetails);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/contested-application-not-approved-submit", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Creates application not approved entry and document. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> createGeneralLetter(
        @NotNull @RequestBody @ApiParam("CaseData") String source) {
        CallbackRequest callback = callbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for storing general order with Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        FinremCaseData caseData = contestedNotApprovedService.populateRefusalOrderCollection(caseDetails);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    @PostMapping(path = "/contested-application-send-refusal", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Sends refusal reason to paper cases")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> sendRefusalReason(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

        CallbackRequest callback = callbackRequestDeserializer.deserialize(source);

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for send refusal reason for case with Case ID: {}", caseDetails.getId());

        validateCaseData(callback);

        Optional<Document> refusalReason = contestedNotApprovedService.getLatestRefusalReason(caseDetails);

        if (refusalReason.isPresent()) {
            if (paperNotificationService.shouldPrintForApplicant(caseDetails)) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken,
                    singletonList(documentHelper.getDocumentAsBulkPrintDocument(refusalReason.get()).orElse(null)));
            }

            if (paperNotificationService.shouldPrintForRespondent(caseDetails)) {
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken,
                    singletonList(documentHelper.getDocumentAsBulkPrintDocument(refusalReason.get()).orElse(null)));
            }
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getCaseData()).build());
    }
}
