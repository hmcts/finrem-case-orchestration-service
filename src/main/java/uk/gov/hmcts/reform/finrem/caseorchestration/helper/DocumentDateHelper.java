package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseDocumentTabData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

public abstract class DocumentDateHelper<T extends CaseDocumentTabData> {

    private final ObjectMapper mapper;
    private final Class<T> documentType;

    private final BiPredicate<String, List<T>> isNewDocument = (id, oldDocuments) ->
        oldDocuments.stream().map(T::getElementId).noneMatch(oldId -> oldId.equals(id));

    public DocumentDateHelper(ObjectMapper objectMapper, Class<T> documentType) {
        objectMapper.registerModule(new JavaTimeModule());
        this.mapper = objectMapper;
        this.documentType = documentType;
    }

    public Map<String, Object> addUploadDateToNewDocuments(Map<String, Object> caseData,
                                                           Map<String, Object> caseDataBefore,
                                                           String documentCollection) {
        List<T> allDocuments = getDocumentCollection(caseData, documentCollection);
        List<T> documentsBeforeEvent = getDocumentCollection(caseDataBefore, documentCollection);

        List<T> modifiedDocuments = allDocuments.stream()
            .peek(document -> addDateToNewDocuments(documentsBeforeEvent, document))
            .toList();

        caseData.put(documentCollection, modifiedDocuments);
        return caseData;
    }

    private void addDateToNewDocuments(List<T> documentsBeforeEvent, T document) {
        if (isNewDocument.test(document.getElementId(), documentsBeforeEvent)) {
            document.setUploadDateTime(LocalDateTime.now());
        }
    }

    private List<T> getDocumentCollection(Map<String, Object> caseData,
                                            String documentCollection) {
        List<T> documentList = mapper.convertValue(caseData.get(documentCollection),
            TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, documentType));

        return Optional.ofNullable(documentList).orElse(Collections.emptyList());
    }
}
