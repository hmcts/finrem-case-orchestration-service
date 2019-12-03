package uk.gov.hmcts.reform.finrem.caseorchestration.controllers.bulkscan;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.update.in.BulkScanCaseUpdateRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.update.out.SuccessfulUpdateResponse;

import javax.validation.Valid;
import java.util.Collections;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
public class UpdateCaseController {

    private static final String CASE_TYPE_ID = "FINANCIAL_REMEDY";
    private static final String EVENT_ID = "EVENT_ID";
    private static final String CREATE_EVENT_ID = "bulkScanCaseCreate";
    private static final String UPDATE_EVENT_ID = "bulkScanCaseUpdate";
    private static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";

    @PostMapping(
            path = "/update-case",
            consumes = APPLICATION_JSON,
            produces = APPLICATION_JSON
        )
    @ApiOperation(value = "API to update Financial Remedy case data by bulk scan")
    @ApiResponses({
            @ApiResponse(code = 200, response = SuccessfulUpdateResponse.class,
                    message = "Update of case data has been successful"
            ),
            @ApiResponse(code = 400, message = "Request failed due to malformed syntax (and only for that reason). "
                    + "This response results in a general error presented to the caseworker in CCD"),
            @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
            @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
            @ApiResponse(code = 422, message = "Exception record is well-formed, but contains invalid data.")
    })
    public ResponseEntity<SuccessfulUpdateResponse> updateCase(
            @RequestHeader(name = SERVICE_AUTHORISATION_HEADER, required = false) String s2sAuthToken,
            @Valid @RequestBody BulkScanCaseUpdateRequest request
    ) {
        log.info("Updates existing case based on exception record");

        // Commenting out until S2S Auth is enabled for FR
        // authService.assertIsServiceAllowedToUpdate(s2sAuthToken);

        SuccessfulUpdateResponse callbackResponse = SuccessfulUpdateResponse.builder()
            .caseUpdateDetails(
                CaseCreationDetails
                    .builder()
                    .caseData(request.getCaseData())
                    .caseTypeId(CASE_TYPE_ID)
                    .eventId(UPDATE_EVENT_ID)
                    .build()
            ).warnings(Collections.emptyList())
            .build();

        return ResponseEntity.ok(callbackResponse);
    }
}
