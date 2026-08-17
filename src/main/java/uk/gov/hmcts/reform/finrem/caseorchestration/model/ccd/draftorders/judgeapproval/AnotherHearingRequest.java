package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.judgeapproval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingTimeDirection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_anotherHearingRequest", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnotherHearingRequest {

    @CCD(label = "Which order is this hearing for?", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList whichOrder;

    @CCD(
            label = "Type of Hearing",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_ManageHearingType"
    )
    private HearingType typeOfHearing;

    @CCD(label = "Time Estimate", searchable = false)
    private HearingTimeDirection timeEstimate;

    @CCD(label = "Additional Time", searchable = false)
    private String additionalTime;

    @CCD(label = "Any other listing instructions", searchable = false, typeOverride = FieldType.TextArea)
    private String anyOtherListingInstructions;
}
