package uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ParentLetterDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties
public class IntervenerRemovedLetterDetails implements ParentLetterDetails {
    private FrcCourtDetails courtDetails;
    private Addressee addressee;
    private String caseNumber;
    private String reference;
    private String divorceCaseNumber;
    private String letterDate;
    private String applicantName;
    private String respondentName;
    private String intervenerFullName;
}
