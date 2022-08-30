package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseDocumentData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public abstract class DocumentDateHelper<T extends CaseDocumentData> {

    protected final ObjectMapper mapper;

    private final BiPredicate<String, List<T>> isAnyMatchingId = (id, oldDocuments) ->
        oldDocuments.stream().map(T::getElementId).anyMatch(oldId -> oldId.equals(id));

    public DocumentDateHelper(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        this.mapper = objectMapper;
    }

    public abstract Map<String, Object> addUploadDateToNewDocuments(Map<String, Object> caseData,
                                                                    Map<String, Object> caseDataBefore,
                                                                    String documentCollection);

    protected void addDateToNewDocuments(List<T> documentsBeforeEvent, T document) {
        if (isAnyMatchingId.negate().test(document.getElementId(), documentsBeforeEvent)) {
            document.setUploadDateTime(LocalDateTime.now());
        }
    }

    protected List<T> getDocumentCollection(Map<String, Object> caseData, String documentCollection, Class<T> typeClass) {
        return mapper.convertValue(caseData.get(documentCollection),
            TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, typeClass));
    }
}
