package uk.gov.hmcts.reform.finrem.caseorchestration.controllers.bulkscan;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataValidationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanValidationService;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
public class OcrValidationController {

    private final AuthService authService;
    private final BulkScanValidationService bulkScanValidationService;

    public OcrValidationController(
            AuthService authService,
            BulkScanValidationService bulkScanValidationService
    ) {
        this.authService = authService;
        this.bulkScanValidationService = bulkScanValidationService;
    }

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
        log.info("Request {}", request);
        log.info("Request ocrDataFields", request.getOcrDataFields());

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
}
