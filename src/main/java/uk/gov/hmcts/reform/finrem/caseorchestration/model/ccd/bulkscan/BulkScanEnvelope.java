package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.bulkscan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BulkScanEnvelope {
    private final String id;
    private final String action;
}
