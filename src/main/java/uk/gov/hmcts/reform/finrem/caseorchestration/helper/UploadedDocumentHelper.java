package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedUploadedDocumentData;

@Component
public class UploadedDocumentHelper extends DocumentDateHelper<ContestedUploadedDocumentData> {

    public UploadedDocumentHelper(ObjectMapper objectMapper) {
        super(objectMapper, ContestedUploadedDocumentData.class);
    }
}
