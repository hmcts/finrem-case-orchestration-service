package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseDocumentData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public abstract class DocumentDateHelper<T extends CaseDocumentData> {

    protected final ObjectMapper mapper;
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
        List<T> allDocuments = Optional.ofNullable(getDocumentCollection(caseData, documentCollection))
            .orElse(Collections.emptyList());

        List<T> oldDocuments = Optional.ofNullable(getDocumentCollection(caseDataBefore, documentCollection))
            .orElse(Collections.emptyList());

        List<T> modifiedDocuments = allDocuments.stream()
            .peek(document -> addDateToNewDocuments(oldDocuments, document))
            .collect(Collectors.toList());

        caseData.put(documentCollection, modifiedDocuments);
        return caseData;
    }

    protected void addDateToNewDocuments(List<T> documentsBeforeEvent, T document) {
        if (isNewDocument.test(document.getElementId(), documentsBeforeEvent)) {
            document.setUploadDateTime(LocalDateTime.now());
        }
    }

    protected List<T> getDocumentCollection(Map<String, Object> caseData,
                                            String documentCollection) {
        return mapper.convertValue(caseData.get(documentCollection),
            TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, documentType));
    }
}
