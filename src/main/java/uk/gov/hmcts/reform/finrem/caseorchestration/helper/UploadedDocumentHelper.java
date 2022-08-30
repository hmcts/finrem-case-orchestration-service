package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class UploadedDocumentHelper extends DocumentDateHelper<ContestedUploadedDocumentData> {

    public UploadedDocumentHelper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public Map<String, Object> addUploadDateToNewDocuments(Map<String, Object> caseData,
                                                           Map<String, Object> caseDataBefore,
                                                           String documentCollection) {

        List<ContestedUploadedDocumentData> allDocuments = Optional.ofNullable(
            getDocumentCollection(caseData, documentCollection, ContestedUploadedDocumentData.class))
            .orElse(Collections.emptyList());

        List<ContestedUploadedDocumentData> documentsBeforeEvent = Optional.ofNullable(
            getDocumentCollection(caseDataBefore, documentCollection, ContestedUploadedDocumentData.class))
            .orElse(Collections.emptyList());

        List<ContestedUploadedDocumentData> modifiedDocuments = allDocuments.stream()
            .peek(document -> addDateToNewDocuments(documentsBeforeEvent, document))
            .toList();

        caseData.put(documentCollection, modifiedDocuments);
        return caseData;
    }
}
