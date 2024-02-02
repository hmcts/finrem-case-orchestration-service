package uk.gov.hmcts.reform.finrem.caseorchestration.handler.shareselecteddocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;

import java.util.List;

@Service
public class ShareSelectedDocumentsMidEventHandler extends FinremCallbackHandler {

    private final AssignCaseAccessService assignCaseAccessService;

    private final IntervenerShareDocumentsService intervenerShareDocumentsService;

    public ShareSelectedDocumentsMidEventHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                 AssignCaseAccessService assignCaseAccessService,
                                                 IntervenerShareDocumentsService intervenerShareDocumentsService) {
        super(finremCaseDetailsMapper);
        this.assignCaseAccessService = assignCaseAccessService;
        this.intervenerShareDocumentsService = intervenerShareDocumentsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType) && CaseType.CONTESTED.equals(caseType)
            && EventType.SHARE_SELECTED_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequestWithFinremCaseDetails, String userAuthorisation) {
        Long caseId = callbackRequestWithFinremCaseDetails.getCaseDetails().getId();
        String activeUser = assignCaseAccessService.getActiveUserCaseRole(caseId.toString(), userAuthorisation);

        FinremCaseData caseData = callbackRequestWithFinremCaseDetails.getCaseDetails().getData();
        caseData.setCurrentUserCaseRoleType(activeUser);
        List<String> errors = intervenerShareDocumentsService.checkThatApplicantAndRespondentAreBothSelected(caseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }
}
