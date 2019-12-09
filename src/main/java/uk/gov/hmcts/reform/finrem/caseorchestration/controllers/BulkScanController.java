package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataValidationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.update.in.transformation.in.BulkScanCaseUpdateRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.update.in.transformation.output.SuccessfulUpdateResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformer.FrFormToCaseTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanValidationService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@Controller
public class BulkScanController {

    private static final String CASE_TYPE_ID = "FINANCIAL_REMEDY";
    // will remove this below
    private static final String EVENT_ID = "EVENT_ID";
    private static final String CREATE_EVENT_ID = "bulkScanCaseCreate";
    private static final String UPDATE_EVENT_ID = "bulkScanCaseUpdate";
    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";

    @Autowired
    private BulkScanValidationService bulkScanValidationService;

    @Autowired
    private AuthService authService;

    @PostMapping(
        path = "/forms/{form-type}/validate-ocr",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation("Validates OCR form data based on form type")
    @ApiResponses({
        @ApiResponse(
            code = 200, response = OcrValidationResponse.class, message = "Validation executed successfully"
        ),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "S2S token is not authorized to use the service"),
        @ApiResponse(code = 404, message = "Form type not found")
    })
    public ResponseEntity<OcrValidationResponse> validateOcrData(
        @RequestHeader(name = "ServiceAuthorization", required = false) String serviceAuthHeader,
        @PathVariable(name = "form-type", required = false) String formType,
        @Valid @RequestBody OcrDataValidationRequest request
    ) {
        String serviceName = authService.authenticate(serviceAuthHeader);
        log.info("Request received to validate ocr data from service {}", serviceName);

        try {
            OcrValidationResult ocrValidationResult = bulkScanValidationService.validate(
                formType, request.getOcrDataFields()
            );
            return ok().body(new OcrValidationResponse(ocrValidationResult));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
        log.info("Transforming exception record to case");

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
        log.info("Updates existing case based on exception record");

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
