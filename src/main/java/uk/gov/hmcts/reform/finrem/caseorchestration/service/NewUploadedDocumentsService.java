package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadingDocumentAccessor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
public class NewUploadedDocumentsService {

    /**
     * Retrieves the list of newly uploaded documents by comparing the current and previous case data.
     *
     * <p>
     * This method extracts documents from the provided {@code FinremCaseData} instances using the given accessor
     * and determines which documents are newly uploaded (i.e., present in {@code caseData} but not in {@code caseDataBefore}).
     * </p>
     *
     * @param <T>           The type of document accessor, which extends {@link UploadingDocumentAccessor}.
     * @param caseData      The current {@link FinremCaseData} instance containing the latest uploaded documents.
     * @param caseDataBefore The previous {@link FinremCaseData} instance to compare against.
     * @param accessor      A function that extracts a list of {@code UploadingDocumentAccessor} instances from {@link FinremCaseData}.
     * @return A list of {@link CaseDocument} instances that are newly uploaded (i.e., present in {@code caseData} but not in {@code caseDataBefore}).
     */
    public <T extends UploadingDocumentAccessor<?>> List<CaseDocument> getNewUploadDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore,
                                                                                             Function<FinremCaseData, List<T>> accessor) {
        List<T> uploadedDocuments = accessor.apply(caseData);
        List<T> previousDocuments = accessor.apply(caseDataBefore);

        if (isEmpty(uploadedDocuments)) {
            return Collections.emptyList();
        } else if (isEmpty(previousDocuments)) {
            return extractCaseDocuments(uploadedDocuments);
        }

        return extractNewCaseDocuments(uploadedDocuments, previousDocuments);
    }

    private List<CaseDocument> extractCaseDocuments(List<? extends UploadingDocumentAccessor<?>> documents) {
        return documents.stream()
            .map(UploadingDocumentAccessor::getValue)
            .filter(Objects::nonNull)
            .map(HasUploadingDocuments::getUploadingDocuments)
            .flatMap(List::stream)
            .toList();
    }

    private List<CaseDocument> extractNewCaseDocuments(List<? extends UploadingDocumentAccessor<?>> uploadedDocuments,
                                                       List<? extends UploadingDocumentAccessor<?>> previousDocuments) {
        Set<CaseDocument> previousSet = new HashSet<>(extractCaseDocuments(previousDocuments));

        return extractCaseDocuments(uploadedDocuments).stream().filter(doc -> !previousSet.contains(doc)).toList();
    }
}
