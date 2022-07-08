package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_MANAGE_CASE_DOCUMENT_COLLECTION;

public abstract class CaseDocumentHandler<T> {

    private final ObjectMapper objectMapper;

    public CaseDocumentHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected List<T> getDocumentCollection(Map<String, Object> caseData, String collection) {
        if (StringUtils.isEmpty(caseData.get(collection))) {
            return new ArrayList<>();
        }
        return objectMapper.convertValue(caseData.get(collection), new TypeReference<>() {
        });
    }

    public abstract void handle(List<ContestedUploadedDocumentData> uploadedDocuments,
                         Map<String, Object> caseData);

    public static void setDocumentUploadDate(List<ContestedUploadedDocumentData> collection) {

        for (Iterator<ContestedUploadedDocumentData> it = collection.iterator(); it.hasNext(); ) {
            ContestedUploadedDocumentData document = it.next();

            ContestedUploadedDocument contestedUploadedDocument = document.getUploadedCaseDocument();
            contestedUploadedDocument.setDocumentUploadDate(contestedUploadedDocument.getDocumentUploadDate()
                == null ? LocalDate.now() : contestedUploadedDocument.getDocumentUploadDate());
            document.setUploadedCaseDocument(contestedUploadedDocument);
        }

        Comparator<ContestedUploadedDocumentData> mostRecentDocuments =
            Comparator.comparing(t -> t.getUploadedCaseDocument().getDocumentUploadDate(),
                Comparator.nullsLast(Comparator.naturalOrder()));

        collection.sort(mostRecentDocuments.reversed());
    }
}
