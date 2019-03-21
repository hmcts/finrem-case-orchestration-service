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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentService;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.APPLICATION_ISSUED;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class DocumentController implements BaseController {

    @Autowired
    private DocumentService service;

    @PostMapping(path = "/generate-mini-form-a", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Mini Form A generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = CCDCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> generateMiniFormA(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @RequestBody @ApiParam("CaseData") CCDRequest request) {

        log.info("Received request for generateMiniFormA. Auth token: {}, Case request : {}", authorisationToken,
                request);
        validateCaseData(request);
        CaseData caseData = request.getCaseDetails().getCaseData();
        CaseDocument document = service.generateMiniFormA(authorisationToken, request.getCaseDetails());
        caseData.setMiniFormA(document);
        caseData.setState(APPLICATION_ISSUED.toString());

        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());
    }


    @PostMapping(path = "/v2/generate-mini-form-a",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Mini Form A generation. Serves as a callback from CCD")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
                    + "attached to the case",
                    response = CCDCallbackResponse.class),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateMiniFormA(
            @RequestHeader(value = "Authorization") String authorisationToken,
            @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        Map<String, Object> caseData = callback.getCaseDetails().getData();
        CaseDocument document = service.generateMiniFormA(authorisationToken, callback.getCaseDetails());
        caseData.put("miniFormA", document);
        caseData.put("state", APPLICATION_ISSUED.toString());

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }
}
