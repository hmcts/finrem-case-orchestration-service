package uk.gov.hmcts.reform.finrem.caseorchestration.model.barristers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ParentLetterDetails;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BarristerLetterDetails implements ParentLetterDetails {
    private Map<String, Object> courtDetails;
    private Addressee addressee;
    private String caseNumber;
    private String reference;
    private String divorceCaseNumber;
    private String letterDate;
    private String applicantName;
    private String respondentName;
    private String barristerFirmName;
}
