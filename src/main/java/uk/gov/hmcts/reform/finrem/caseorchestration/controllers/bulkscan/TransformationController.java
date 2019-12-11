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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformer.FrFormToCaseTransformer;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
public class TransformationController {

    private final AuthService authService;

    private static final String CASE_TYPE_ID = "FINANCIAL_REMEDY";
    private static final String EVENT_ID = "EVENT_ID";

    private FrFormToCaseTransformer frFormToCaseTransformer = new FrFormToCaseTransformer();

    @PostMapping(
            path = "/transform-exception-record",
            consumes = APPLICATION_JSON,
            produces = APPLICATION_JSON
        )
    @ApiOperation(value = "Transform exception record into case data if valid.")
    @ApiResponses({
            @ApiResponse(code = 200, response = SuccessfulTransformationResponse.class,
                    message = "Transformation of exception record into case data has been successful"
                    ),
            @ApiResponse(code = 400, message = "Request failed due to malformed syntax (and only for that reason)"),
            @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
            @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
            @ApiResponse(code = 422, message = "Exception record is well-formed, but contains invalid data.")
            })
    public ResponseEntity<SuccessfulTransformationResponse> transformExceptionRecordToCase(
            @RequestHeader(name = "ServiceAuthorization", required = false) String serviceAuthHeader,
            @Valid @RequestBody ExceptionRecord exceptionRecord
    ) {
        authService.assertIsServiceAllowedToValidate(serviceAuthHeader);

        Map<String, Object> transformedCaseData = frFormToCaseTransformer.transformIntoCaseData(exceptionRecord);

        SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
                .caseCreationDetails(
                        new CaseCreationDetails(
                                CASE_TYPE_ID,
                                EVENT_ID,
                                transformedCaseData))
                .warnings(Collections.emptyList())
                .build();

        return ResponseEntity.ok(callbackResponse);
    }
}
