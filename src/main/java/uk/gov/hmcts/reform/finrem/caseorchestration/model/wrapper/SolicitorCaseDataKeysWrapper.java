package uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SolicitorCaseDataKeysWrapper {
    private String solicitorReferenceKey;
    private String solicitorNameKey;
    private String solicitorEmailKey;
}
