package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.MiamCheckService;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class MiamCheckController implements BaseController {

    @Autowired
    private MiamCheckService service;

    @PostMapping(path = "/miam-attend-exempt-check", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Application cannot be made unless the applicant has either attended, or is exempt.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully.",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateContestedMiniFormA(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        log.info("Received request for validating MIAM exemption. Auth token: {}, Case request : {}",
                authorisationToken, callback);

        validateCaseData(callback);
        return ResponseEntity.ok(response(callback));
    }

    private AboutToStartOrSubmitCallbackResponse response(CallbackRequest callback) {
        List<String> errors = service.miamExemptAttendCheck(callback.getCaseDetails());
        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }
}
