package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_c_judgeNotApprovedReasons", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgeNotApprovedReason {
    @CCD(
            label = "Please specify why you are unable to approve the draft directions order",
            hint = "A Case Worker Will contact the Solicitor to ask for an amended draft",
            searchable = false
    )
    private String judgeNotApprovedReasons;
}
