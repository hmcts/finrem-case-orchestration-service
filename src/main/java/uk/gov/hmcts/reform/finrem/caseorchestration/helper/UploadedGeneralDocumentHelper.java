package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UploadedGeneralDocumentHelper {

    private final ObjectMapper mapper;
    final BiPredicate<String, List<GeneralUploadedDocumentData>> isAnyIdMatches = (id, oldDocuments) ->
        oldDocuments.stream().map(GeneralUploadedDocumentData::getId).anyMatch(oldId -> oldId.equals(id));

    public Map<String, Object> addUploadDateToNewDocuments(Map<String, Object> caseData,
                                                           Map<String, Object> caseDataBefore,
                                                           String documentCollection) {

        List<GeneralUploadedDocumentData> allDocuments = mapper.convertValue(
            caseData.get(documentCollection), new TypeReference<>() {
            });

        List<GeneralUploadedDocumentData> oldDocuments = mapper.convertValue(
            caseDataBefore.get(documentCollection), new TypeReference<>() {
            });

        List<GeneralUploadedDocumentData> newDocuments = allDocuments;
        if (oldDocuments != null) {
            newDocuments = allDocuments.stream()
                .filter(document -> isAnyIdMatches.negate().test(document.getId(), oldDocuments))
                .collect(Collectors.toList());
        }
        newDocuments.forEach(document -> document.getGeneralUploadedDocument().setGeneralDocumentUploadDateTime(LocalDateTime.now()));

        if (oldDocuments != null) {
            newDocuments.addAll(oldDocuments);
        }
        caseData.put(documentCollection, newDocuments);

        return caseData;
    }
}
