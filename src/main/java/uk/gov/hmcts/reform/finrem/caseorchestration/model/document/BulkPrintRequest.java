package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class BulkPrintRequest {
    private String caseId;
    private String letterType;
    private List<BulkPrintDocument> bulkPrintDocuments;
}
