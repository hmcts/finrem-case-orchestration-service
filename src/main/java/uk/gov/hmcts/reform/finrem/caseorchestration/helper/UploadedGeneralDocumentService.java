package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralUploadedDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentUploadService;

@Component
public class UploadedGeneralDocumentService extends DocumentUploadService<GeneralUploadedDocumentData> {

    public UploadedGeneralDocumentService(ObjectMapper objectMapper) {
        super(objectMapper, GeneralUploadedDocumentData.class);
    }
}
