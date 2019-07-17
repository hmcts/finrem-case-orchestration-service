package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class DocumentValidationResponse {
    private String mimeType;
    private List<String> errors;
}
