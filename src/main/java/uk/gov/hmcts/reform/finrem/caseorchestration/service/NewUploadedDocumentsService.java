package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadingDocumentAccessor;

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
     *
     * @param <T>              the type of document wrapper, extending {@link UploadingDocumentAccessor}
     * @param caseData         the current {@link FinremCaseData} containing the latest uploaded documents
     * @param caseDataBefore   the previous {@link FinremCaseData} for comparison
     * @param accessor         a function to extract the list of documents from the case data
     * @return a list of newly uploaded documents that were not present in the previous case data
     */
    public <T extends UploadingDocumentAccessor<?>> List<T> getNewUploadDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore,
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
            .filter(d -> d.getValue() != null && d.getValue().getUploadingDocument() != null)
            .forEach(d -> {
                boolean exists = previousDocuments.stream()
                    .filter(pd -> pd.getValue() != null && pd.getValue().getUploadingDocument() != null)
                    .anyMatch(pd -> nullSafeCaseDocument(pd.getValue().getUploadingDocument()).getDocumentUrl().equals(
                        nullSafeCaseDocument(d.getValue().getUploadingDocument()).getDocumentUrl()));
                if (!exists) {
                    ret.add(d);
                }
            });

        return ret;
    }
}
