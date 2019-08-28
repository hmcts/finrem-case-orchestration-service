package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentValidationService;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.builder;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class DocumentValidationController implements BaseController {

    @Autowired
    private DocumentValidationService service;

    @PostMapping(path = "/field/{field}/file-upload-check", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Checks the file type and returns error.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully.",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> checkUploadedFileType(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callbackRequest,
            @PathVariable("field") String field) {

        log.info("Received request for checkUploadedFileType. Auth token: {}, Case request : {}",
                authorisationToken, callbackRequest);
        validateCaseData(callbackRequest);
        return ResponseEntity.ok(response(callbackRequest, field, authorisationToken));
    }

    private AboutToStartOrSubmitCallbackResponse response(CallbackRequest callbackRequest, String field,
                                                          String authorisationToken) {
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        AboutToStartOrSubmitCallbackResponseBuilder builder = builder();
        if (nonNull(caseData.get(field))) {
            DocumentValidationResponse response = service.validateDocument(callbackRequest, field, authorisationToken);
            return builder.errors(response.getErrors()).build();
        }
        return builder().build();
    }
}
