package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
public class NewUploadedDocumentsService {

    /**
     * Retrieves the newly uploaded documents by comparing the current and previous case data.
     *
     * @param caseData the current case data containing the uploaded documents.
     * @param caseDataBefore the previous case data to compare against.
     * @param getDocumentsFromCaseData a function to extract the list of documents from the case data.
     * @param <T> a type that extends HasUploadingDocuments, representing documents with upload details.
     * @return a list of newly uploaded CaseDocument objects.
     */
    public <T extends HasUploadingDocuments> List<CaseDocument> getNewUploadDocuments(
        FinremCaseData caseData, FinremCaseData caseDataBefore, Function<FinremCaseData, List<T>> getDocumentsFromCaseData) {

        List<T> uploadedDocuments = emptyIfNull(getDocumentsFromCaseData.apply(caseData));
        List<T> previousDocuments = emptyIfNull(getDocumentsFromCaseData.apply(caseDataBefore));

        if (isEmpty(uploadedDocuments)) {
            return Collections.emptyList();
        } else if (isEmpty(previousDocuments)) {
            return extractCaseDocuments(uploadedDocuments);
        }

        return extractNewCaseDocuments(uploadedDocuments, previousDocuments);
    }

    private List<CaseDocument> extractCaseDocuments(List<? extends HasUploadingDocuments> documents) {
        return documents.stream()
            .filter(Objects::nonNull)
            .map(HasUploadingDocuments::getUploadingDocuments)
            .flatMap(List::stream)
            .toList();
    }

    private List<CaseDocument> extractNewCaseDocuments(List<? extends HasUploadingDocuments> uploadedDocuments,
                                                       List<? extends HasUploadingDocuments> previousDocuments) {
        Set<CaseDocument> previousSet = new HashSet<>(extractCaseDocuments(previousDocuments));
        return extractCaseDocuments(uploadedDocuments).stream().filter(doc -> !previousSet.contains(doc)).toList();
    }
}
