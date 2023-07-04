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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedConfidentialDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONFIDENTIAL_DOCS_UPLOADED_COLLECTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadConfidentialDocumentsAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final ObjectMapper objectMapper;
    private final UploadedConfidentialDocumentService uploadedConfidentialDocumentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && EventType.UPLOAD_CONFIDENTIAL_DOCUMENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        Map<String, Object> caseData = uploadedConfidentialDocumentHelper.addUploadDateToNewDocuments(
            callbackRequest.getCaseDetails().getData(),
            callbackRequest.getCaseDetailsBefore().getData(),
            CONFIDENTIAL_DOCS_UPLOADED_COLLECTION);

        List<ConfidentialUploadedDocumentData> uploadedDocuments = getConfidentialDocumentCollection(caseData);

        uploadedDocuments.sort(comparing(ConfidentialUploadedDocumentData::getValue,
            comparing(UploadConfidentialDocument::getConfidentialDocumentUploadDateTime, nullsLast(Comparator.reverseOrder()))));

        caseData.put(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION, uploadedDocuments);

        return GenericAboutToStartOrSubmitCallbackResponse
            .<Map<String, Object>>builder()
            .data(caseData)
            .build();
    }

    private List<ConfidentialUploadedDocumentData> getConfidentialDocumentCollection(Map<String, Object> caseData) {
        objectMapper.registerModule(new JavaTimeModule());

        List<ConfidentialUploadedDocumentData> confidentialDocuments = objectMapper
            .convertValue(caseData.get(CONFIDENTIAL_DOCS_UPLOADED_COLLECTION), new TypeReference<>() {
            });

        return Optional.ofNullable(confidentialDocuments).orElse(new ArrayList<>());
    }
}
