package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DraftDirectionDetailsHolder {
    private YesOrNo isThisFinalYN;
    private YesOrNo isAnotherHearingYN;
    private HearingTypeDirection typeOfHearing;
    private HearingTimeDirection timeEstimate;
    private String additionalTime;
    private Court localCourt;
    private NottinghamCourt nottinghamList;
    private CfcCourt cfcList;
    private String listingInstructor;

}
