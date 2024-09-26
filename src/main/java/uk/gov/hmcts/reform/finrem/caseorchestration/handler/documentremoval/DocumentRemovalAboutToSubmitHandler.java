package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentremoval.DocumentRemovalService;


@Slf4j
@Service
public class DocumentRemovalAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentRemovalService documentRemovalService;

    public DocumentRemovalAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                               DocumentRemovalService documentRemovalService) {
        super(mapper);
        this.documentRemovalService = documentRemovalService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType)
                || CaseType.CONSENTED.equals(caseType))
                && (EventType.REMOVE_CASE_DOCUMENT.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();

        log.info("Invoking event document removal about to submit callback for Case ID: {}",
            caseDetails.getId());

        FinremCaseData amendedCaseData =
                documentRemovalService.removeDocuments(caseData, caseDetails.getId(), userAuthorisation);

        log.info("Completed event document removal about to start callback for Case ID: {}",
            caseDetails.getId());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(amendedCaseData).build();
    }

}
