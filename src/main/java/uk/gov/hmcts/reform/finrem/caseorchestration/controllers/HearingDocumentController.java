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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import javax.validation.constraints.NotNull;

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

    private final HearingDocumentService service;
    private final ValidateHearingService validateHearingService;
    private final BulkPrintService bulkPrintService;
    private final DocumentHelper documentHelper;

    @PostMapping(path = "/documents/hearing", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Form C and G generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateHearingDocument(
            @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

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
        caseData.putAll(service.generateHearingDocuments(authorisationToken, caseDetails));

        if (isContestedPaperApplication(caseDetails)) {
            List<BulkPrintDocument> caseDocuments = getHearingCaseDocuments(caseData);
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, caseDocuments);
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, caseDocuments);
        }

        List<String> warnings = validateHearingService.validateHearingWarnings(caseDetails);
        return ResponseEntity.ok(
                AboutToStartOrSubmitCallbackResponse.builder().data(caseData).warnings(warnings).build());
    }

    private List<BulkPrintDocument> getHearingCaseDocuments(Map<String, Object> caseData) {
        List<BulkPrintDocument> caseDocuments = new ArrayList<>();
        caseDocuments.addAll(documentHelper.getCollectionOfDocumentLinksAsBulkPrintDocuments(caseData, "copyOfPaperFormA", "uploadedDocument"));
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, "formC").ifPresent(caseDocuments::add);
        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, "formG").ifPresent(caseDocuments::add);
        return caseDocuments;
    }
}
