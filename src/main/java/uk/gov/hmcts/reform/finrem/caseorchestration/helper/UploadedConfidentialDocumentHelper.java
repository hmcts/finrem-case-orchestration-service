package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class UploadedConfidentialDocumentHelper extends DocumentDateHelper<ConfidentialUploadedDocumentData> {

    public UploadedConfidentialDocumentHelper(ObjectMapper objectMapper) {
        super(objectMapper, ConfidentialUploadedDocumentData.class);
    }
}
