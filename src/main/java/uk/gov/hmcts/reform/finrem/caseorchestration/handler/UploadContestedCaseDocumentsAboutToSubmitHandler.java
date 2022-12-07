package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseDocumentCollectionsManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_UPLOADED_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadContestedCaseDocumentsAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final List<CaseDocumentCollectionsManager> caseDocumentHandlers;
    private final ObjectMapper objectMapper;
    private final UploadedDocumentHelper uploadedDocumentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.UPLOAD_CASE_FILES.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        Map<String, Object> caseData = uploadedDocumentHelper.addUploadDateToNewDocuments(
            callbackRequest.getCaseDetails().getData(),
            callbackRequest.getCaseDetailsBefore().getData(), CONTESTED_UPLOADED_DOCUMENTS);
        List<UploadCaseDocumentCollection> uploadedDocuments = getDocumentCollection(caseData);
        caseDocumentHandlers.stream().forEach(h -> h.addDocumentToCollection(caseData));
        uploadedDocuments.sort(Comparator.comparing(
            UploadCaseDocumentCollection::getUploadCaseDocument, Comparator.comparing(
                UploadCaseDocument::getCaseDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        caseData.put(CONTESTED_UPLOADED_DOCUMENTS, uploadedDocuments);
        return GenericAboutToStartOrSubmitCallbackResponse
            .<Map<String, Object>>builder()
            .data(caseData)
            .build();
    }

    private List<UploadCaseDocumentCollection> getDocumentCollection(Map<String, Object> caseData) {
        objectMapper.registerModule(new JavaTimeModule());

        List<UploadCaseDocumentCollection> contestedUploadDocuments = objectMapper.convertValue(
            caseData.get(CONTESTED_UPLOADED_DOCUMENTS), new TypeReference<>() {
            });

        return Optional.ofNullable(contestedUploadDocuments).orElse(new ArrayList<>());
    }

}
