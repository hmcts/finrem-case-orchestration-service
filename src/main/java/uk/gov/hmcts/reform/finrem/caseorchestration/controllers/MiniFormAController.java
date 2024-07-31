package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MINI_FORM_A_CONSENTED_IN_CONTESTED;

@RestController
@RequestMapping(value = "/case-orchestration")
@RequiredArgsConstructor
@Slf4j
public class MiniFormAController extends BaseController {

    public static final String ASSIGNED_TO_JUDGE_REASON = "assignedToJudgeReason";
    public static final String ASSIGNED_TO_JUDGE_REASON_DEFAULT = "Draft consent order";
    public static final String ASSIGNED_TO_JUDGE = "assignedToJudge";
    public static final String REFER_TO_JUDGE_DATE = "referToJudgeDate";
    public static final String REFER_TO_JUDGE_TEXT = "referToJudgeText";
    public static final String REFER_TO_JUDGE_TEXT_DEFAULT = "consent for approval";

    private final OnlineFormDocumentService service;
    private final DefaultsConfiguration defaultsConfiguration;
    private final CaseDataService caseDataService;

    @PostMapping(path = "/documents/generate-mini-form-a", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles Consented Mini Form A generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback was processed successfully or in case of an error message is attached to the case",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateMiniFormA(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @Parameter(description = "CaseData") CallbackRequest callback) {

        log.info("Received request to generate Consented Mini Form A for Case ID: {}", callback.getCaseDetails().getId());

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
        caseData.put(ASSIGNED_TO_JUDGE, defaultsConfiguration.getAssignedToJudgeDefault());
        if (caseDataService.isConsentedApplication(caseDetails)) {
            caseData.put(ASSIGNED_TO_JUDGE_REASON, ASSIGNED_TO_JUDGE_REASON_DEFAULT);
            caseData.put(REFER_TO_JUDGE_DATE, LocalDate.now());
            caseData.put(REFER_TO_JUDGE_TEXT, REFER_TO_JUDGE_TEXT_DEFAULT);
        }
    }
}
