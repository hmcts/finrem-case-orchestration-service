package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
public class DocumentUploadServiceV2 {

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
        uploadedDocuments.forEach(d -> {
            boolean exists = previousDocuments.stream()
                .anyMatch(pd -> pd.getValue().getDocumentLink().getDocumentUrl().equals(d.getValue().getDocumentLink().getDocumentUrl()));
            if (!exists) {
                ret.add(d);
            }
        });

        return ret;
    }
}
