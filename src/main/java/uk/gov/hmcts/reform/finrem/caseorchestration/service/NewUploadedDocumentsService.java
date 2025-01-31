package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadingDocumentsHolder;

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
     * Retrieves a list of newly uploaded documents by comparing the current and previous case data.
     *
     * @param caseData                 the current case data containing uploaded documents
     * @param caseDataBefore           the previous case data before the latest update
     * @param getDocumentsFromCaseData a function to extract document collections from case data
     * @param <T>                      a type that extends {@link UploadingDocumentsHolder}
     * @return a list of newly uploaded {@link CaseDocument} objects; returns an empty list if no new documents are found
     */
    public <T extends UploadingDocumentsHolder<?>> List<CaseDocument> getNewUploadDocuments(
        FinremCaseData caseData, FinremCaseData caseDataBefore,  Function<FinremCaseData, List<T>> getDocumentsFromCaseData) {

        List<T> uploadedDocuments = getDocumentsFromCaseData.apply(caseData);
        List<T> previousDocuments = getDocumentsFromCaseData.apply(caseDataBefore);

        if (isEmpty(uploadedDocuments)) {
            return Collections.emptyList();
        } else if (isEmpty(previousDocuments)) {
            return extractCaseDocuments(uploadedDocuments);
        }

        return extractNewCaseDocuments(uploadedDocuments, previousDocuments);
    }

    private List<CaseDocument> extractCaseDocuments(List<? extends UploadingDocumentsHolder<?>> documents) {
        return documents.stream()
            .map(UploadingDocumentsHolder::getValue)
            .filter(Objects::nonNull)
            .map(HasUploadingDocuments::getUploadingDocuments)
            .flatMap(List::stream)
            .toList();
    }

    private List<CaseDocument> extractNewCaseDocuments(List<? extends UploadingDocumentsHolder<?>> uploadedDocuments,
                                                       List<? extends UploadingDocumentsHolder<?>> previousDocuments) {
        Set<CaseDocument> previousSet = new HashSet<>(extractCaseDocuments(previousDocuments));

        return extractCaseDocuments(uploadedDocuments).stream().filter(doc -> !previousSet.contains(doc)).toList();
    }
}
