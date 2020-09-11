package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ContestedDraftOrderNotApprovedService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import javax.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class ContestedDraftOrderNotApprovedController implements BaseController {

    @Autowired
    private IdamService idamService;

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private final ContestedDraftOrderNotApprovedService contestedNotApprovedService;

    @PostMapping(path = "/contested-application-not-approved-start", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Clears previous entered field values. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> initialiseApplicationNotApprovedProperties(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request to clear contested application not approved fields for Case ID: {}", caseDetails.getId());

        validateCaseData(callback);

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put("refusalOrderJudgeType", null);
        caseData.put("refusalOrderDate", null);
        caseData.put("refusalOrderPreviewDocument", null);

        caseData.put("refusalOrderJudgeName", idamService.getIdamFullName(authorisationToken));

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
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        log.info("Received request to preview application not approved document for Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        Map<String, Object> refusalOrder = contestedNotApprovedService.createRefusalOrder(authorisationToken, caseDetails);
        caseData.putAll(refusalOrder);

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
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for storing general order with Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        Map<String, Object> caseData = contestedNotApprovedService.populateRefusalOrderCollection(caseDetails);

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
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        CaseDetails caseDetails = callback.getCaseDetails();
        log.info("Received request for send refusal reason for paper cases with Case ID: {}", caseDetails.getId());
        validateCaseData(callback);

        Optional<CaseDocument> refusalReason = contestedNotApprovedService.getLatestRefusalReason(caseDetails);

        if (refusalReason.isPresent()) {
            if (bulkPrintService.shouldPrintForApplicant(caseDetails)) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken,
                    Arrays.asList(bulkPrintService.getBulkPrintDocumentFromCaseDocument(refusalReason.get())));
            }

            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken,
                Arrays.asList(bulkPrintService.getBulkPrintDocumentFromCaseDocument(refusalReason.get())));
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build());
    }
}