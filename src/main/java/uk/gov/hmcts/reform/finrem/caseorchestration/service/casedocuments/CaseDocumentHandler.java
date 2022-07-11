package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CaseDocumentHandler<T> {

    protected final ObjectMapper objectMapper;

    public CaseDocumentHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static void setDocumentUploadDate(List<ContestedUploadedDocumentData> collection) {

        for (Iterator<ContestedUploadedDocumentData> it = collection.iterator(); it.hasNext(); ) {
            ContestedUploadedDocumentData document = it.next();

            ContestedUploadedDocument contestedUploadedDocument = document.getUploadedCaseDocument();
            contestedUploadedDocument.setDocumentUploadDate(contestedUploadedDocument.getDocumentUploadDate()
                == null ? LocalDateTime.now() : contestedUploadedDocument.getDocumentUploadDate());
            document.setUploadedCaseDocument(contestedUploadedDocument);
        }
    }

    protected List<ContestedUploadedDocumentData> sortDocumentsByDateDesc(List<ContestedUploadedDocumentData> collection) {

        Function<ContestedUploadedDocumentData, ContestedUploadedDocument> getDocuments = ContestedUploadedDocumentData::getUploadedCaseDocument;

        return collection.stream().sorted(Comparator.comparing(getDocuments.andThen(ContestedUploadedDocument::getDocumentUploadDate),
            Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
    }

    public abstract void handle(List<ContestedUploadedDocumentData> uploadedDocuments,
                                Map<String, Object> caseData);

    protected abstract List<T> getDocumentCollection(Map<String, Object> caseData, String collection);
}
