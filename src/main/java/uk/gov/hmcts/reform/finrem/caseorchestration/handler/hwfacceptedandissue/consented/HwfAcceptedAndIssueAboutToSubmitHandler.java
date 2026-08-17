package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hwfacceptedandissue.consented;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DefaultsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.IssueApplicationConsentedAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.issueapplication.IssueApplicationService;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AssignToJudgeReason.DRAFT_CONSENT_ORDER;

/**
 * Logics are copied from {@link IssueApplicationConsentedAboutToSubmitHandler}.
 */
@Slf4j
@Service
public class HwfAcceptedAndIssueAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final OnlineFormDocumentService onlineFormDocumentService;
    private final DefaultsConfiguration defaultsConfiguration;
    private final IssueApplicationService issueApplicationService;

    private static final String MISSING_COURT_SELECTION_ERROR = "Case cannot be issued as court selection is missing.";

    public HwfAcceptedAndIssueAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
            OnlineFormDocumentService onlineFormDocumentService,
            DefaultsConfiguration defaultsConfiguration,
            IssueApplicationService issueApplicationService) {
        super(finremCaseDetailsMapper);
        this.onlineFormDocumentService = onlineFormDocumentService;
        this.defaultsConfiguration = defaultsConfiguration;
        this.issueApplicationService = issueApplicationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.HWF_ACCEPTED_AND_ISSUE.equals(eventType);
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
        issueApplicationService.generateCoverSheets(caseDetails, userAuthorisation);
    }
}
