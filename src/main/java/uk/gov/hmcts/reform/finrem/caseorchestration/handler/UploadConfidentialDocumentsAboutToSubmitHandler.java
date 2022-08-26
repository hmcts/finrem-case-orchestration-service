package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedConfidentialDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadConfidentialDocumentsAboutToSubmitHandler implements CallbackHandler {

    private final ObjectMapper objectMapper;
    private final UploadedConfidentialDocumentHelper uploadedConfidentialDocumentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && EventType.UPLOAD_CONFIDENTIAL_DOCUMENT.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {

        Map<String, Object> caseData = uploadedConfidentialDocumentHelper.addUploadDateToNewDocuments(
            callbackRequest.getCaseDetails().getData(),
            callbackRequest.getCaseDetailsBefore().getData(), CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);
        List<ConfidentialUploadedDocumentData> uploadedDocuments = getConfidentialDocumentCollection(caseData, CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);
        uploadedDocuments.sort(Comparator.comparing(
            ConfidentialUploadedDocumentData::getConfidentialUploadedDocument, Comparator.comparing(
                ConfidentialUploadedDocument::getConfidentialDocumentUploadDateTime, Comparator.nullsLast(
                    Comparator.reverseOrder()))));
        caseData.put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, uploadedDocuments);
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .build();
    }

    private List<ConfidentialUploadedDocumentData> getConfidentialDocumentCollection(Map<String, Object> caseData, String collection) {
        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return objectMapper.registerModule(new JavaTimeModule()).convertValue(caseData.get(collection), new TypeReference<>() {});
    }
}
