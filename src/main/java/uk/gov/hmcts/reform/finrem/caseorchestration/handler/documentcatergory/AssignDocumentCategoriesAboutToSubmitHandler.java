package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentcatergory;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DocumentCategoryAssigner;

@Slf4j
@Service
public class AssignDocumentCategoriesAboutToSubmitHandler extends FinremCallbackHandler {

    private final DocumentCategoryAssigner documentCategoryAssigner;

    public AssignDocumentCategoriesAboutToSubmitHandler(
        FinremCaseDetailsMapper finremCaseDetailsMapper, DocumentCategoryAssigner documentCategoryAssigner) {
        super(finremCaseDetailsMapper);
        this.documentCategoryAssigner = documentCategoryAssigner;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.ASSIGN_DOCUMENT_CATEGORIES.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequestWithFinremCaseDetails,
                                                                              String userAuthorisation) {

        log.info("Received request to assign document categories for case with Case ID: {}",
            callbackRequestWithFinremCaseDetails.getCaseDetails().getId());
        documentCategoryAssigner.assignDocumentCategories(callbackRequestWithFinremCaseDetails.getCaseDetails().getData());
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(callbackRequestWithFinremCaseDetails.getCaseDetails().getData()).build();
    }
}
