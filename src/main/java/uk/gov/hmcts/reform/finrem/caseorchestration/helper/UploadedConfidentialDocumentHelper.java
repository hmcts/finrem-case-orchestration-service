package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UploadedConfidentialDocumentHelper {

    private final ObjectMapper mapper;
    final BiPredicate<String, List<ConfidentialUploadedDocumentData>> isAnyIdMatches = (id, oldDocuments) ->
        oldDocuments.stream().map(ConfidentialUploadedDocumentData::getId).anyMatch(oldId -> oldId.equals(id));

    public Map<String, Object> addUploadDateToNewDocuments(Map<String, Object> caseData,
                                                           Map<String, Object> caseDataBefore,
                                                           String documentCollection) {

        List<ConfidentialUploadedDocumentData> allDocuments = mapper.convertValue(
            caseData.get(documentCollection), new TypeReference<>() {
            });

        List<ConfidentialUploadedDocumentData> oldDocuments = mapper.convertValue(
            caseDataBefore.get(documentCollection), new TypeReference<>() {
            });

        List<ConfidentialUploadedDocumentData> newDocuments = allDocuments;
        if (oldDocuments != null) {
            newDocuments = allDocuments.stream()
                .filter(document -> isAnyIdMatches.negate().test(document.getId(), oldDocuments))
                .collect(Collectors.toList());
        }
        newDocuments.forEach(document -> document.getConfidentialUploadedDocument().setConfidentialDocumentUploadDateTime(LocalDateTime.now()));

        if (oldDocuments != null) {
            newDocuments.addAll(oldDocuments);
        }
        caseData.put(documentCollection, newDocuments);

        return caseData;
    }
}
