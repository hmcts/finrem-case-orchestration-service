package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.domain;

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
public class DraftDirectionDetails {
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
