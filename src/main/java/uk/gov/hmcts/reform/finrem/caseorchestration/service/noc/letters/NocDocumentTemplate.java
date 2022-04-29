package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.letters;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NocDocumentTemplate {
    private final String templateName;
    private final String documentFileName;
}
