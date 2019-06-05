package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import javax.validation.constraints.NotNull;

import java.util.Map;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_LETTER_TEXT;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class GeneralLetterValidateController implements BaseController {

    @Value("${generalLetterBody.default.Text}")
    private String generalLetterBodyDefaultText;

    @Value("${generalLetterBody.error.message}")
    private String errorMessage;

    @PostMapping(path = "/general-letter-validate", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Validates general letter text. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> validateGeneralLetterText(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        log.info("Received request for general letter validate. Auth token: {}, Case request : {}",
                authorisationToken, callback);

        validateCaseData(callback);
        Map<String,Object> caseData = callback.getCaseDetails().getData();

        return Optional.of((String)caseData.get(GENERAL_LETTER_TEXT))
                .filter(this::checkTextPredicate)
                .map(this::sendError)
                .orElseGet(this::sendDefault);
    }

    private boolean checkTextPredicate(String text) {
        return text.equals(generalLetterBodyDefaultText);
    }

    private ResponseEntity sendError(String text) {
        log.info("general letter text {} is invalid.", text);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .errors(ImmutableList.of(errorMessage))
                .build());
    }

    private ResponseEntity sendDefault() {
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().build());
    }
}
