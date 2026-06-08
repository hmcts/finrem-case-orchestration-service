package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AssignToJudgeReason.DRAFT_CONSENT_ORDER;

@Slf4j
@Service
public class IssueApplicationConsentedAboutToSubmitHandler extends FinremCallbackHandler {

    private final OnlineFormDocumentService onlineFormDocumentService;
    private final DefaultsConfiguration defaultsConfiguration;
    private final GenerateCoverSheetService generateCoverSheetService;

    private static final String MISSING_COURT_SELECTION_ERROR = "Case cannot be issued as court selection is missing.";

    public IssueApplicationConsentedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         OnlineFormDocumentService onlineFormDocumentService,
                                                         DefaultsConfiguration defaultsConfiguration,
                                                         GenerateCoverSheetService generateCoverSheetService) {
        super(finremCaseDetailsMapper);
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.defaultsConfiguration = defaultsConfiguration;
        this.generateCoverSheetService = generateCoverSheetService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.ISSUE_APPLICATION.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        try {
            generateCoverSheets(caseDetails, userAuthorisation);
        } catch (MissingCourtException e) {
            return response(caseData, null, List.of(MISSING_COURT_SELECTION_ERROR));
        }

        caseData.setMiniFormA(onlineFormDocumentService.generateMiniFormA(userAuthorisation, caseDetails));
        populateAssignToJudgeFields(caseData);

        return response(caseData);
    }

    private void populateAssignToJudgeFields(FinremCaseData caseData) {
        caseData.setAssignedToJudge(defaultsConfiguration.getAssignedToJudgeDefault());
        caseData.setAssignedToJudgeReason(DRAFT_CONSENT_ORDER);
        caseData.getReferToJudgeWrapper().setReferToJudgeDate(LocalDate.now());
        caseData.getReferToJudgeWrapper().setReferToJudgeText("consent for approval");
    }

    private void generateCoverSheets(FinremCaseDetails caseDetails, String userAuthorisation) {
        generateCoverSheetService.generateAndSetApplicantCoverSheet(caseDetails, userAuthorisation);
        generateCoverSheetService.generateAndSetRespondentCoverSheet(caseDetails, userAuthorisation);
    }
}
