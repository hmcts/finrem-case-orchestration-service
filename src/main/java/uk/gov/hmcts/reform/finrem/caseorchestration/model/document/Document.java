package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Document {
    private String url;
    private String mimeType;
    private String createdOn;
    private String fileName;
    private String binaryUrl;
}
