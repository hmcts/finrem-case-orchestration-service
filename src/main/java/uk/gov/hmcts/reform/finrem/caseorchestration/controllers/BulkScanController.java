package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.bsp.common.config.BulkScanEndpoints;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.service.AuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.bulkscan.BulkScanEvents;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkScanService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_FR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BulkScanController {

    @Autowired
    private BulkScanService bulkScanService;

    @Autowired
    private AuthService authService;

    @PostMapping(
        path = BulkScanEndpoints.VALIDATE,
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
        @RequestHeader(name = SERVICE_AUTHORISATION_HEADER) String s2sAuthToken,
        @PathVariable(name = "form-type") String formType,
        @Valid @RequestBody OcrDataValidationRequest request
    ) {
        log.info("Validating form {} for bulk scanning operation", formType);

        authService.assertIsServiceAllowedToValidate(s2sAuthToken);

        ResponseEntity<OcrValidationResponse> response;

        try {
            OcrValidationResponse ocrValidationResponse = validateExceptionRecord(formType, request.getOcrDataFields());
            response = ok().body(ocrValidationResponse);
        } catch (UnsupportedFormTypeException unsupportedFormTypeException) {
            log.error(unsupportedFormTypeException.getMessage(), unsupportedFormTypeException);
            response = ResponseEntity.notFound().build();
        }

        return response;
    }

    @PostMapping(
        path = BulkScanEndpoints.TRANSFORM,
        consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON
    )
    @ApiOperation(value = "Transform exception record into CCD case data")
    @ApiResponses({
        @ApiResponse(code = 200, response = SuccessfulTransformationResponse.class,
            message = "Transformation of exception record into case data has been successful"
        ),
        @ApiResponse(code = 400, message = "Request failed due to malformed syntax (and only for that reason)"),
        @ApiResponse(code = 401, message = "Provided S2S token is missing or invalid"),
        @ApiResponse(code = 403, message = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(code = 422, message = "Exception record is well-formed, but contains invalid data.")
    })
    public ResponseEntity<SuccessfulTransformationResponse> transformExceptionRecordIntoCase(
        @RequestHeader(name = SERVICE_AUTHORISATION_HEADER) String s2sAuthToken,
        @Valid @RequestBody ExceptionRecord exceptionRecord
    ) {
        log.info("Transforming exception record to case");

        authService.assertIsServiceAllowedToUpdate(s2sAuthToken);

        ResponseEntity<SuccessfulTransformationResponse> controllerResponse;
        try {
            OcrValidationResponse ocrValidationResponse = validateExceptionRecord(
                exceptionRecord.getFormType(), exceptionRecord.getOcrDataFields()
            );

            Map<String, Object> transformedCaseData = bulkScanService.transformBulkScanForm(exceptionRecord);

            SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
                .warnings(ocrValidationResponse.getWarnings())
                .caseCreationDetails(
                    new CaseCreationDetails(
                        CASE_TYPE_ID_FR,
                        BulkScanEvents.CREATE.getEventName(),
                        transformedCaseData
                    )
                )
                .build();

            controllerResponse = ok(callbackResponse);
        } catch (UnsupportedFormTypeException exception) {
            controllerResponse = ResponseEntity.unprocessableEntity().build();
        }

        return controllerResponse;
    }

    private OcrValidationResponse validateExceptionRecord(String formType, List<OcrDataField> ocrDataFields) {
        return new OcrValidationResponse(bulkScanService.validateBulkScanForm(formType, ocrDataFields));
    }
}
