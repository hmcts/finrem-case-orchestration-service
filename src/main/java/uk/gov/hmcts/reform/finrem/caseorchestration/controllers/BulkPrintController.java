package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.print.BulkPrintMetadata;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;

import javax.validation.constraints.NotNull;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class BulkPrintController implements BaseController {

    @Autowired
    private BulkPrintService bulkPrintService;

    @PostMapping(path = "/bulk-print", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles bulk print")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200,
                message = "Callback was processed successfully or in case of an error message is attached to the case",
                response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> bulkPrint(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @NotNull @RequestBody @ApiParam("Callback") CallbackRequest callback) {

        log.info("Received request for Bulk Print for Case ID");

        validateCaseData(callback);

        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (BulkPrintService.shouldBeSentToApplicant(ImmutableMap.copyOf(caseDetails.getData()))) {
            BulkPrintMetadata applicantBulkPrintData = bulkPrintService.sendLetterToApplicant(authToken, caseDetails);

            caseData.putAll(applicantBulkPrintData.toMap(BULK_PRINT_COVER_SHEET_APP, BULK_PRINT_LETTER_ID_APP));
        }

        BulkPrintMetadata respondentBulkPrintData = bulkPrintService.sendLetterToRespondent(authToken, caseDetails);
        caseData.putAll(respondentBulkPrintData.toMap(BULK_PRINT_COVER_SHEET_RES, BULK_PRINT_LETTER_ID_RES));

        caseData.remove(BULK_PRINT_COVER_SHEET);
        log.info("Bulk print is successful.");

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
