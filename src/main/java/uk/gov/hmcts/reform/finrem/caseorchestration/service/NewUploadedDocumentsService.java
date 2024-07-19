package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
public class NewUploadedDocumentsService {

    private CaseDocument nullSafeCaseDocument(CaseDocument caseDocument) {
        return ofNullable(caseDocument).orElse(CaseDocument.builder().build());
    }

    /**
     * Retrieves a list of newly uploaded documents by comparing the current case data with the previous case data.
     * The existence of caseDocumentLink is the key factor of the comparison.
     *
     * @param <T> the type parameter extending {@code CaseDocumentCollection}
     * @param caseData the current {@code FinremCaseData}
     * @param caseDataBefore the previous {@code FinremCaseData}
     * @param accessor a function that extracts the list of documents from the {@code FinremCaseData}
     * @return a list of newly uploaded documents, or an empty list if no new documents are found
     */
    public <T extends CaseDocumentCollection<?>> List<T> getNewUploadDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore,
                                                                               Function<FinremCaseData, List<T>> accessor) {
        List<T> uploadedDocuments = accessor.apply(caseData);
        List<T> previousDocuments = accessor.apply(caseDataBefore);

        if (isEmpty(uploadedDocuments)) {
            return Collections.emptyList();
        } else if (isEmpty(previousDocuments)) {
            return uploadedDocuments;
        }

        List<T> ret = new ArrayList<>();
        uploadedDocuments.stream()
            .filter(d -> d.getValue() != null && d.getValue().getDocumentLink() != null)
            .forEach(d -> {
                boolean exists = previousDocuments.stream()
                    .filter(pd -> pd.getValue() != null && pd.getValue().getDocumentLink() != null)
                    .anyMatch(pd -> nullSafeCaseDocument(pd.getValue().getDocumentLink()).getDocumentUrl().equals(
                        nullSafeCaseDocument(d.getValue().getDocumentLink()).getDocumentUrl()));
                if (!exists) {
                    ret.add(d);
                }
            });

        return ret;
    }
}
