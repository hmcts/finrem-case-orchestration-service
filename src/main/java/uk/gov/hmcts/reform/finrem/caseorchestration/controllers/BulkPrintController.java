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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_RES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.getValue;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class BulkPrintController implements BaseController {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateCoverSheetService coverSheetService;

    @PostMapping(
        path = "/bulk-print",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles bulk print")
    @ApiResponses(
        value = {
            @ApiResponse(
                code = 200,
                message =
                    "Callback was processed successFully or in case of an error message is "
                        + "attached to the case",
                response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> bulkPrint(
        @RequestHeader(value = "Authorization", required = false) String authorisationToken,
        @NotNull @RequestBody @ApiParam("Callback") CallbackRequest callback) {

        log.info(
            "Received request for bulk print. Auth token: {}, Case request : {}",
            authorisationToken,
            callback);

        validateCaseData(callback);

        Map<String, Object> caseData = callback.getCaseDetails().getData();

        CaseDocument coverSheetRes = coverSheetService
            .generateRespondentCoverSheet(callback.getCaseDetails(), authorisationToken);

        UUID letterIdRes = bulkPrintService.sendForBulkPrint(coverSheetRes, callback.getCaseDetails());

        caseData.put(BULK_PRINT_COVER_SHEET_RES, coverSheetRes);

        caseData.put(BULK_PRINT_LETTER_ID_RES, letterIdRes);
        log.info(
                "Respondent coversheet: {}, Respondent letterId: {}",
                coverSheetRes,
                letterIdRes);

        String solicitorAgreeToReceiveEmails =  (String) getValue
            .apply(callback.getCaseDetails().getData(), "solicitorAgreeToReceiveEmails").orElse("");

        String applicantRepresented = (String) getValue
            .apply(callback.getCaseDetails().getData(), "applicantRepresented").orElse("");
        log.info(
                "Applicant solicitorAgreeToReceiveEmails : {}, Applicant applicantRepresented: {}",
                solicitorAgreeToReceiveEmails,
                applicantRepresented);

        if ( "No".equalsIgnoreCase(applicantRepresented) || "No".equalsIgnoreCase(solicitorAgreeToReceiveEmails) ) {
            CaseDocument coverSheetApp = coverSheetService
                .generateApplicantCoverSheet(callback.getCaseDetails(), authorisationToken);

            UUID letterIdApp = bulkPrintService.sendForBulkPrint(coverSheetApp, callback.getCaseDetails());

            caseData.put(BULK_PRINT_COVER_SHEET_APP, coverSheetApp);

            caseData.put(BULK_PRINT_LETTER_ID_APP, letterIdApp);

            log.info(
                    "Applicant coversheet: {}, Applicant letterId: {}",
                    coverSheetApp,
                    letterIdApp);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData)
            .build());
    }
}
