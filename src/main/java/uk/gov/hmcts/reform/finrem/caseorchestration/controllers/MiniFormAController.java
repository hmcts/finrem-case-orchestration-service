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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A_CONSENTED_IN_CONTESTED;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
public class MiniFormAController extends BaseController {

    @Autowired private OnlineFormDocumentService service;
    @Autowired private DefaultsConfiguration defaultsConfiguration;
    @Autowired private CaseDataService caseDataService;

    public static final String assignedToJudgeReason = "assignedToJudgeReason";
    public static final String assignedToJudgeReasonDefault = "Draft consent order";
    public static final String assignedToJudge = "assignedToJudge";
    public static final String referToJudgeDate = "referToJudgeDate";
    public static final String referToJudgeText = "referToJudgeText";
    public static final String referToJudgeTextDefault = "consent for approval";

    @PostMapping(path = "/documents/generate-mini-form-a", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Consented Mini Form A generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateMiniFormA(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") CallbackRequest callback) {

        log.info("Received request to generate Consented Mini Form A for Case ID : {}", callback.getCaseDetails().getId());

        CaseDetails caseDetails = callback.getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        if (!caseDataService.isConsentedInContestedCase(caseDetails)) {
            CaseDocument document = service.generateMiniFormA(authorisationToken, callback.getCaseDetails());
            caseData.put(MINI_FORM_A, document);

            log.info("Defaulting AssignedToJudge fields for Case ID: {}", callback.getCaseDetails().getId());
            populateAssignToJudgeFields(caseData, caseDetails);
        } else {
            CaseDocument document = service.generateConsentedInContestedMiniFormA(callback.getCaseDetails(), authorisationToken);
            caseData.put(MINI_FORM_A_CONSENTED_IN_CONTESTED, document);
        }

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void populateAssignToJudgeFields(Map<String, Object> caseData, CaseDetails caseDetails) {
        caseData.put(assignedToJudge, defaultsConfiguration.getAssignedToJudgeDefault());
        if (caseDataService.isConsentedApplication(caseDetails)) {
            caseData.put(assignedToJudgeReason, assignedToJudgeReasonDefault);
            caseData.put(referToJudgeDate, LocalDate.now());
            caseData.put(referToJudgeText, referToJudgeTextDefault);
        }
    }
}
