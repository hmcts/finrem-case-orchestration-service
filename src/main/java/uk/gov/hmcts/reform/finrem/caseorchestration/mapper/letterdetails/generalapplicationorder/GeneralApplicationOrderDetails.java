package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.generalapplicationorder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralApplicationOrderDetails implements DocumentTemplateDetails {
    @JsonProperty("courtDetails")
    private FrcCourtDetails courtDetails;
    @JsonProperty("applicantName")
    private String applicantName;
    @JsonProperty("respondentName")
    private String respondentName;
    @JsonProperty("letterDate")
    private String letterDate;
}
