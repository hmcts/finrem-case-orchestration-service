package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HelpWithFeesSuccessLetter extends DataForTemplate {
    private final String caseNumber;
    private final String reference;
    private final Addressee addressee;
    private final String letterDate;
    private final String applicantName;
    private final String respondentName;
}
