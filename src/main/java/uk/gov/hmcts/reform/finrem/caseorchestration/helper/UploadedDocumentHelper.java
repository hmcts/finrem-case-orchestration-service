package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UploadedDocumentHelper {

    private final ObjectMapper mapper;
    final BiPredicate<String, List<ContestedUploadedDocumentData>> isAnyIdMatches = (id, oldDocuments) ->
        oldDocuments.stream().map(ContestedUploadedDocumentData::getId).anyMatch(oldId -> oldId.equals(id));

    public Map<String, Object> addUploadDateToNewDocuments(Map<String, Object> caseData,
                                                           Map<String, Object> caseDataBefore,
                                                           String documentCollection) {

        List<ContestedUploadedDocumentData> allDocuments = mapper.convertValue(
            caseData.get(documentCollection), new TypeReference<>() {
            });

        List<ContestedUploadedDocumentData> oldDocuments = mapper.convertValue(
            caseDataBefore.get(documentCollection), new TypeReference<>() {
            });

        List<ContestedUploadedDocumentData> newDocuments = allDocuments.stream()
            .filter(document -> isAnyIdMatches.negate().test(document.getId(), oldDocuments))
            .collect(Collectors.toList());

        newDocuments.forEach(document -> document.getUploadedCaseDocument().setUploadDateTime(LocalDateTime.now()));
        oldDocuments.addAll(newDocuments);
        caseData.put(documentCollection, oldDocuments);

        return caseData;
    }
}
