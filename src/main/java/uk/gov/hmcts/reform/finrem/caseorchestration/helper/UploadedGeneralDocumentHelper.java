package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_UPLOADED_DOCUMENTS;

@Component
public class UploadedGeneralDocumentHelper extends DocumentDateHelper<GeneralUploadedDocumentData> {

    public UploadedGeneralDocumentHelper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public Map<String, Object> addUploadDateToNewDocuments(Map<String, Object> caseData,
                                                           Map<String, Object> caseDataBefore,
                                                           String documentCollection) {

        List<GeneralUploadedDocumentData> allDocuments = Optional.ofNullable(
            getDocumentCollection(caseData, documentCollection, GeneralUploadedDocumentData.class))
            .orElse(Collections.emptyList());

        List<GeneralUploadedDocumentData> documentsBeforeEvent = Optional.ofNullable(
            getDocumentCollection(caseDataBefore, documentCollection, GeneralUploadedDocumentData.class))
            .orElse(Collections.emptyList());

        List<GeneralUploadedDocumentData> modifiedDocuments = allDocuments.stream()
            .peek(document -> addDateToNewDocuments(documentsBeforeEvent, document))
            .collect(Collectors.toList());

        caseData.put(GENERAL_UPLOADED_DOCUMENTS, modifiedDocuments);

        return caseData;
    }
}
