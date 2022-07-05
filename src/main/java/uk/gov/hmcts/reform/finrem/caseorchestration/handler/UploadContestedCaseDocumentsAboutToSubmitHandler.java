package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentHandler;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.UploadCaseDocumentCollection;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadContestedCaseDocumentsAboutToSubmitHandler implements CallbackHandler {

    private final List<CaseDocumentHandler> caseDocumentHandlers;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_CASE_FILES.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {

        FinremCaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        List<UploadCaseDocumentCollection> uploadedDocuments = caseData.getUploadCaseDocumentWrapper().getUploadCaseDocument();
        caseDocumentHandlers.stream().forEach(h -> h.handle(uploadedDocuments, caseData));
        caseData.getUploadCaseDocumentWrapper().setUploadCaseDocument(uploadedDocuments);
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .build();
    }
}
