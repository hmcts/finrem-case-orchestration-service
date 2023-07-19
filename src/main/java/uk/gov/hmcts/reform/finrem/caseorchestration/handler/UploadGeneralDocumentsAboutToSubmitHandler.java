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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedGeneralDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_UPLOADED_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadGeneralDocumentsAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final ObjectMapper objectMapper;
    private final UploadedGeneralDocumentHelper uploadedGeneralDocumentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && EventType.UPLOAD_GENERAL_DOCUMENT.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        Map<String, Object> caseData = uploadedGeneralDocumentHelper.addUploadDateToNewDocuments(
            callbackRequest.getCaseDetails().getData(),
            callbackRequest.getCaseDetailsBefore().getData(),
            GENERAL_UPLOADED_DOCUMENTS);

        List<GeneralUploadedDocumentData> uploadedDocuments = getGeneralDocumentCollection(caseData);

        uploadedDocuments.sort(comparing(GeneralUploadedDocumentData::getGeneralUploadedDocument,
            comparing(GeneralUploadedDocument::getGeneralDocumentUploadDateTime, nullsLast(Comparator.reverseOrder()))));

        caseData.put(GENERAL_UPLOADED_DOCUMENTS, uploadedDocuments);
        return GenericAboutToStartOrSubmitCallbackResponse
            .<Map<String, Object>>builder()
            .data(caseData)
            .build();
    }

    private List<GeneralUploadedDocumentData> getGeneralDocumentCollection(Map<String, Object> caseData) {
        objectMapper.registerModule(new JavaTimeModule());

        List<GeneralUploadedDocumentData> generalDocuments = objectMapper.convertValue(
            caseData.get(GENERAL_UPLOADED_DOCUMENTS), new TypeReference<>() {
            });

        return Optional.ofNullable(generalDocuments).orElse(Collections.emptyList());
    }
}
