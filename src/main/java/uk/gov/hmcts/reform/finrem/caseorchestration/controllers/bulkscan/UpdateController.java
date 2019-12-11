package uk.gov.hmcts.reform.finrem.caseorchestration.controllers.bulkscan;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.bsp.common.service.AuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.update.in.transformation.in.BulkScanCaseUpdateRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.update.in.transformation.output.SuccessfulUpdateResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformer.FrFormToCaseTransformer;

import javax.validation.Valid;
import java.util.Collections;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
public class UpdateController {

    private final AuthService authService;

    private static final String CASE_TYPE_ID = "FINANCIAL_REMEDY";
    private static final String UPDATE_EVENT_ID = "bulkScanCaseUpdate";

    private FrFormToCaseTransformer frFormToCaseTransformer = new FrFormToCaseTransformer();

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
            @RequestHeader(name = "ServiceAuthorization", required = false) String serviceAuthHeader,
            @Valid @RequestBody BulkScanCaseUpdateRequest request
    ) {
        authService.assertIsServiceAllowedToValidate(serviceAuthHeader);

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