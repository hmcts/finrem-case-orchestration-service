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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class BulkPrintController implements BaseController {

    @Autowired
    private BulkPrintService service;

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
    public ResponseEntity<SubmittedCallbackResponse> bulkPrint(
        @RequestHeader(value = "Authorization", required = false) String authorisationToken,
        @NotNull @RequestBody @ApiParam("Callback") CallbackRequest callback) {

        log.info(
            "Received request for bulk print. Auth token: {}, Case request : {}",
            authorisationToken,
            callback);

        validateCaseData(callback);

        service.sendForBulkPrint(callback.getCaseDetails(), authorisationToken);

        return ResponseEntity.ok(SubmittedCallbackResponse.builder().confirmationBody("Bulk print is successful.")
                .build());
    }
}
