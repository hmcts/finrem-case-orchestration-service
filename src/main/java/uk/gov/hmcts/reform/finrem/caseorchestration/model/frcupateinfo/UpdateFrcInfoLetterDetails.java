package uk.gov.hmcts.reform.finrem.caseorchestration.model.frcupateinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateFrcInfoLetterDetails {
    private Map<String, Object> courtDetails;
    private Addressee addressee;
    private String caseNumber;
    private String reference;
    private String divorceCaseNumber;
    private String letterDate;
    private String applicantName;
    private String respondentName;
}
