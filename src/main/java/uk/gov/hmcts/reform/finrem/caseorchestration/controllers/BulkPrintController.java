package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import javax.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_AGREE_TO_RECEIVE_EMAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class BulkPrintController implements BaseController {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateCoverSheetService coverSheetService;

    private CaseDetails caseDetails;

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

        caseDetails = callback.getCaseDetails();

        generateCoversheetForApplicant(authToken);
        generateCoversheetForRespondent(authToken);

        Map<String, Object> caseData = caseDetails.getData();
        caseData.remove(BULK_PRINT_COVER_SHEET);
        log.info("Bulk print is successful.");

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void generateCoversheetForApplicant(String authToken) {

        Map<String, Object> caseData = caseDetails.getData();

        if (applicantIsNotRepresentedByASolicitor(caseData) || solicitorDidNotAgreeToReceiveEmails(caseData)) {

            CaseDocument applicantCoverSheet = coverSheetService.generateApplicantCoverSheet(caseDetails, authToken);
            UUID applicantLetterId = bulkPrintService.sendForBulkPrint(applicantCoverSheet, caseDetails);

            caseData.put(BULK_PRINT_COVER_SHEET_APP, applicantCoverSheet);
            caseData.put(BULK_PRINT_LETTER_ID_APP, applicantLetterId);

            log.info("Generated Applicant CoverSheet for bulk print. coversheet: {}, letterId : {}", applicantCoverSheet, applicantLetterId);
        }
    }

    private void generateCoversheetForRespondent(String authToken) {

        CaseDocument respondentCoverSheet = coverSheetService.generateRespondentCoverSheet(caseDetails, authToken);
        UUID respondentLetterId = bulkPrintService.sendForBulkPrint(respondentCoverSheet, caseDetails);

        Map<String, Object> caseData = caseDetails.getData();
        caseData.put(BULK_PRINT_COVER_SHEET_RES, respondentCoverSheet);
        caseData.put(BULK_PRINT_LETTER_ID_RES, respondentLetterId);

        log.info("Generated Respondent CoverSheet for bulk print. coversheet: {}, letterId : {}", respondentCoverSheet, respondentLetterId);
    }

    private boolean applicantIsNotRepresentedByASolicitor(Map<String, Object> caseData) {

        return NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APPLICANT_REPRESENTED)));
    }

    private boolean solicitorDidNotAgreeToReceiveEmails(Map<String, Object> caseData) {

        return NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(SOLICITOR_AGREE_TO_RECEIVE_EMAILS)));
    }
}
