package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentClientDocument {

    private String url;
    private String fileName;
    private String binaryUrl;
}
