package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadConfidentialDocumentCollection;

@Component
public class UploadedConfidentialDocumentHelper extends DocumentDateHelper<UploadConfidentialDocumentCollection> {

    public UploadedConfidentialDocumentHelper(ObjectMapper objectMapper) {
        super(objectMapper, UploadConfidentialDocumentCollection.class);
    }
}
