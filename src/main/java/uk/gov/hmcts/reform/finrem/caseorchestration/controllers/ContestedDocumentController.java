package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class ContestedDocumentController implements BaseController {

    @Autowired
    private OnlineFormDocumentService service;

    @PostMapping(path = "/documents/generate-contested-mini-form-a", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Mini Form A generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = AboutToStartOrSubmitCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateContestedMiniFormA(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        Map<String, Object> caseData = callback.getCaseDetails().getData();
        CaseDocument document = service.generateContestedMiniFormA(authorisationToken, callback.getCaseDetails());
        caseData.put(MINI_FORM_A, document);
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
