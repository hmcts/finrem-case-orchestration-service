package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class BulkPrintDocument {
    private String binaryFileUrl;
}
