package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UploadedConfidentialDocumentHelper extends DocumentDateHelper<ConfidentialUploadedDocumentData> {

    public UploadedConfidentialDocumentHelper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public Map<String, Object> addUploadDateToNewDocuments(Map<String, Object> caseData,
                                                           Map<String, Object> caseDataBefore,
                                                           String documentCollection) {
        List<ConfidentialUploadedDocumentData> allDocuments = Optional.ofNullable(
            getDocumentCollection(caseData, documentCollection, ConfidentialUploadedDocumentData.class))
            .orElse(Collections.emptyList());

        List<ConfidentialUploadedDocumentData> oldDocuments = Optional.ofNullable(
            getDocumentCollection(caseDataBefore, documentCollection, ConfidentialUploadedDocumentData.class))
            .orElse(Collections.emptyList());

        List<ConfidentialUploadedDocumentData> modifiedDocuments = allDocuments.stream()
            .peek(document -> addDateToNewDocuments(oldDocuments, document))
            .collect(Collectors.toList());

        caseData.put(documentCollection, modifiedDocuments);
        return caseData;
    }
}
