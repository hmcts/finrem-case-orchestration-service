package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.AssignToJudgeReason;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import javax.validation.constraints.NotNull;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@RestController
@RequestMapping(value = "/case-orchestration")
@Slf4j
@RequiredArgsConstructor
public class  MiniFormAController extends BaseController {

    private final OnlineFormDocumentService service;
    private final DefaultsConfiguration defaultsConfiguration;
    private final FinremCallbackRequestDeserializer finremCallbackRequestDeserializer;


    public static final AssignToJudgeReason ASSIGNED_TO_JUDGE_REASON_DEFAULT = AssignToJudgeReason.DRAFT_CONSENT_ORDER;
    public static final String REFER_TO_JUDGE_TEXT_DEFAULT = "consent for approval";

    @PostMapping(path = "/documents/generate-mini-form-a", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Handles Consented Mini Form A generation. Serves as a callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successfully or in case of an error message is attached to the case",
            response = AboutToStartOrSubmitCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")})
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> generateMiniFormA(
        @RequestHeader(value = AUTHORIZATION_HEADER) String authorisationToken,
        @NotNull @RequestBody @ApiParam("CaseData") String source) {

        CallbackRequest callback = finremCallbackRequestDeserializer.deserialize(source);
        validateCaseData(callback);

        log.info("Received request to generate Consented Mini Form A for Case ID : {}", callback.getCaseDetails().getId());

        FinremCaseDetails caseDetails = callback.getCaseDetails();
        FinremCaseData caseData = caseDetails.getCaseData();

        if (!caseData.isConsentedInContestedCase()) {
            Document miniFormA = service.generateMiniFormA(authorisationToken, caseDetails);
            caseData.setMiniFormA(miniFormA);

            log.info("Defaulting AssignedToJudge fields for Case ID: {}", callback.getCaseDetails().getId());
        } else {
            Document consentMiniFormA = service.generateConsentedInContestedMiniFormA(callback.getCaseDetails(), authorisationToken);
            caseData.getConsentOrderWrapper().setConsentMiniFormA(consentMiniFormA);
        }

        populateAssignToJudgeFields(caseData);

        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());
    }

    private void populateAssignToJudgeFields(FinremCaseData caseData) {
        caseData.setAssignedToJudge(defaultsConfiguration.getAssignedToJudgeDefault());
        if (caseData.isConsentedApplication()) {
            caseData.setAssignedToJudgeReason(ASSIGNED_TO_JUDGE_REASON_DEFAULT);
            caseData.getReferToJudgeWrapper().setReferToJudgeDate(LocalDate.now());
            caseData.getReferToJudgeWrapper().setReferToJudgeText(REFER_TO_JUDGE_TEXT_DEFAULT);
        }
    }
}
