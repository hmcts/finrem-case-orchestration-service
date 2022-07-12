package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.bsp.common.config.BulkScanEndpoints;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.bsp.common.model.update.output.SuccessfulUpdateResponse;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.service.AuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.event.bulkscan.BulkScanEvents;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkScanService;

import javax.validation.Valid;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BulkScanController {

    @Autowired
    private BulkScanService bulkScanService;

    @Autowired
    private AuthService authService;

    @PostMapping(path = BulkScanEndpoints.VALIDATE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Validates OCR form data based on form type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation executed successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OcrValidationResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Provided S2S token is missing or invalid"),
        @ApiResponse(responseCode = "403", description = "S2S token is not authorized to use the service"),
        @ApiResponse(responseCode = "404", description = "Form type not found")})
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
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Transform exception record into CCD case data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "Transformation of exception record into case data has been successful",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulTransformationResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Request failed due to malformed syntax (and only for that reason)"),
        @ApiResponse(responseCode = "401", description = "Provided S2S token is missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "422", description = "Exception record is well-formed, but contains invalid data.")})
    public ResponseEntity<SuccessfulTransformationResponse> transformExceptionRecordIntoCase(
        @RequestHeader(name = SERVICE_AUTHORISATION_HEADER) String s2sAuthToken,
        @Valid @RequestBody ExceptionRecord exceptionRecord
    ) {
        String exceptionRecordId = exceptionRecord.getId();
        log.info("Transforming exception record to case. Id: {}", exceptionRecordId);

        authService.assertIsServiceAllowedToUpdate(s2sAuthToken);

        ResponseEntity<SuccessfulTransformationResponse> controllerResponse;
        try {
            Map<String, Object> transformedCaseData = bulkScanService.transformBulkScanForm(exceptionRecord);

            SuccessfulTransformationResponse callbackResponse = SuccessfulTransformationResponse.builder()
                .caseCreationDetails(
                    new CaseCreationDetails(
                        CASE_TYPE_ID_CONSENTED,
                        BulkScanEvents.CREATE.getEventName(),
                        transformedCaseData))
                .build();

            controllerResponse = ok(callbackResponse);
        } catch (UnsupportedFormTypeException exception) {
            log.error(format("Error transforming exception record. Exception record id is %s", exceptionRecordId), exception);
            controllerResponse = ResponseEntity.unprocessableEntity().build();
        }

        return controllerResponse;
    }

    @PostMapping(
        path = BulkScanEndpoints.UPDATE,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "OUT OF SCOPE: API to update Financial Remedy case data by bulk scan")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Update of case data has been successful",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SuccessfulUpdateResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Request failed due to malformed syntax (and only for that reason). \"\n"
            + "            + \"This response results in a general error presented to the caseworker in CCD"),
        @ApiResponse(responseCode = "401", description = "Provided S2S token is missing or invalid"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "422", description = "Exception record is well-formed, but contains invalid data.")
    })
    public ResponseEntity<SuccessfulUpdateResponse> updateCase(
        @RequestHeader(name = SERVICE_AUTHORISATION_HEADER) String s2sAuthToken,
        @Valid @RequestBody Object request
    ) {
        log.warn("Bulk scan /POST update is not implemented for fin-rem cos");

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    private OcrValidationResponse validateExceptionRecord(String formType, List<OcrDataField> ocrDataFields) {
        return new OcrValidationResponse(bulkScanService.validateBulkScanForm(formType, ocrDataFields));
    }
}
