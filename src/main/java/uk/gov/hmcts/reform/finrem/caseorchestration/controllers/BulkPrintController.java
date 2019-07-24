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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_COVER_SHEET;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;

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
        CaseDocument coverSheetDocument = coverSheetService.generateCoverSheet(callback.getCaseDetails(),
            authorisationToken);

        UUID letterId = bulkPrintService.sendForBulkPrint(coverSheetDocument, callback.getCaseDetails());

        caseData.put(BULK_PRINT_COVER_SHEET, coverSheetDocument);

        caseData.put(BULK_PRINT_LETTER_ID, letterId);

        log.info("Current CaseDetails state: {} and case data state ", callback.getCaseDetails().getState(),
            caseData.get(STATE));

        caseData.put(STATE, callback.getCaseDetails().getState());

        if (callback.getCaseDetails().getState().equals(ConsentedStatus.CONSENT_ORDER_NOT_APPROVED.getId())) {
            log.info("switching to: {}", ConsentedStatus.AWAITING_RESPONSE.getId());
            caseData.put(STATE, ConsentedStatus.AWAITING_RESPONSE.getId());
        }

        if (callback.getCaseDetails().getState().equals(ConsentedStatus.CONSENT_ORDER_APPROVED.getId())) {
            log.info("switching to: {}", ConsentedStatus.CONSENT_ORDER_MADE.getId());
            caseData.put(STATE, ConsentedStatus.CONSENT_ORDER_MADE.getId());
        }

        log.info("Current case state: {} and end case state {} ", callback.getCaseDetails().getState(),
            caseData.get(STATE));

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData)
            .build());
    }
}
