package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingInstruction {

    @JsonProperty("requireAnotherHearing")
    private YesOrNo requireAnotherHearing;

    @JsonProperty("showRequireAnotherHearingQuestion")
    private YesOrNo showRequireAnotherHearingQuestion;

    @JsonProperty("anotherHearingRequestCollection")
    private List<AnotherHearingRequestCollection> anotherHearingRequestCollection;

}
