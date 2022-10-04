package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;

@Component
public class UploadedGeneralDocumentHelper extends DocumentDateHelper<GeneralUploadedDocumentData> {

    public UploadedGeneralDocumentHelper(ObjectMapper objectMapper) {
        super(objectMapper, GeneralUploadedDocumentData.class);
    }
}
