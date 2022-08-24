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
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.UploadedDocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_UPLOADED_DOCUMENTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadGeneralDocumentsAboutToSubmitHandler implements CallbackHandler {

    private final ObjectMapper objectMapper;
    private final UploadedDocumentHelper uploadedDocumentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && EventType.UPLOAD_GENERAL_DOCUMENT.equals(eventType);
    }

    @Override
    public AboutToStartOrSubmitCallbackResponse handle(CallbackRequest callbackRequest, String userAuthorisation) {

        Map<String, Object> caseData = uploadedDocumentHelper.addUploadDateToNewDocuments(
            callbackRequest.getCaseDetails().getData(),
            callbackRequest.getCaseDetailsBefore().getData(), GENERAL_UPLOADED_DOCUMENTS);
        List<GeneralUploadedDocumentData> uploadedDocuments = getGeneralDocumentCollection(caseData, GENERAL_UPLOADED_DOCUMENTS);
        Collections.sort(uploadedDocuments, Comparator.nullsLast((e1, e2) -> e2.getGeneralUploadedDocument()
            .getGeneralDocumentUploadDateTime()
            .compareTo(e1.getGeneralUploadedDocument().getGeneralDocumentUploadDateTime())));
        caseData.put(GENERAL_UPLOADED_DOCUMENTS, uploadedDocuments);
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseData)
            .build();
    }

    private List<GeneralUploadedDocumentData> getGeneralDocumentCollection(Map<String, Object> caseData, String collection) {
        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }

        return objectMapper.registerModule(new JavaTimeModule()).convertValue(caseData.get(collection), new TypeReference<>() {});
    }
}
