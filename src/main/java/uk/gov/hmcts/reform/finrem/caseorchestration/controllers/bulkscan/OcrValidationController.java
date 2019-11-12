package uk.gov.hmcts.reform.finrem.caseorchestration.controllers.bulkscan;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataValidationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AuthService;

import javax.validation.Valid;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.ValidationStatus.SUCCESS;

@RestController
public class OcrValidationController {
    private static final Logger logger = getLogger(OcrValidationController.class);

    private final AuthService authService;

    public OcrValidationController(
            AuthService authService
    ) {
        this.authService = authService;
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
        logger.info("Request received to validate ocr data from service {}", serviceName);

        authService.assertIsAllowedService(serviceName);

        return ok().body(new OcrValidationResponse(emptyList(), emptyList(), SUCCESS));
    }

}
