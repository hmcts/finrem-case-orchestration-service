package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConfidentialUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentUploadService;

@Service
public class UploadedConfidentialDocumentService extends DocumentUploadService<ConfidentialUploadedDocumentData> {

    public UploadedConfidentialDocumentService(ObjectMapper objectMapper) {
        super(objectMapper, ConfidentialUploadedDocumentData.class);
    }
}
