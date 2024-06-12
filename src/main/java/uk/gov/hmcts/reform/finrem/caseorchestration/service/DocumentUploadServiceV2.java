package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadGeneralDocumentCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
public class DocumentUploadServiceV2 {
    
    public List<UploadGeneralDocumentCollection> getNewUploadGeneralDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        List<UploadGeneralDocumentCollection> uploadedDocuments = caseData.getUploadGeneralDocuments();
        List<UploadGeneralDocumentCollection> previousDocuments = caseDataBefore.getUploadGeneralDocuments();

        if (isEmpty(uploadedDocuments)) {
            return Collections.emptyList();
        } else if (isEmpty(previousDocuments)) {
            return uploadedDocuments;
        }

        List<UploadGeneralDocumentCollection> newlyUploadedDocuments = new ArrayList<>();
        uploadedDocuments.forEach(d -> {
            boolean exists = previousDocuments.stream()
                .anyMatch(pd -> pd.getValue().getDocumentLink().getDocumentUrl().equals(d.getValue().getDocumentLink().getDocumentUrl()));
            if (!exists) {
                newlyUploadedDocuments.add(d);
            }
        });

        return newlyUploadedDocuments;
    }

    public List<UploadDocumentCollection> getNewUploadDocuments(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        List<UploadDocumentCollection> uploadedDocuments = caseData.getUploadDocuments();
        List<UploadDocumentCollection> previousDocuments = caseDataBefore.getUploadDocuments();

        if (isEmpty(uploadedDocuments)) {
            return Collections.emptyList();
        } else if (isEmpty(previousDocuments)) {
            return uploadedDocuments;
        }

        List<UploadDocumentCollection> newlyUploadedDocuments = new ArrayList<>();
        uploadedDocuments.forEach(d -> {
            boolean exists = previousDocuments.stream()
                .anyMatch(pd -> pd.getValue().getDocumentLink().getDocumentUrl().equals(d.getValue().getDocumentLink().getDocumentUrl()));
            if (!exists) {
                newlyUploadedDocuments.add(d);
            }
        });

        return newlyUploadedDocuments;
    }
}
