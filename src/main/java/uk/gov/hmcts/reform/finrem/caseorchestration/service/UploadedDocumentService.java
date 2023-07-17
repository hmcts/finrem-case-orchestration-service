package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

@Service
public class UploadedDocumentService extends DocumentDateService<UploadCaseDocumentCollection> {

    public UploadedDocumentService(ObjectMapper objectMapper) {
        super(objectMapper, UploadCaseDocumentCollection.class);
    }
}
