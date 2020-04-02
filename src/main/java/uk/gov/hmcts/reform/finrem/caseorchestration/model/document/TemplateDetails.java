package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class TemplateDetails {

    private String template;
    private String fileName;
}
