package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.bulkscan;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BulkScanEnvelopeCollectionItem {
    private UUID id;
    private BulkScanEnvelope value;
}
