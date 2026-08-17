package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ConsentNatureOfApplication6", generate = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum ConsentNatureOfApplication {
    @CCD(label = "For a stepchild or step children")
    STEP_CHILD_OR_STEP_CHILDREN("Step Child or Step Children"),
    @CCD(label = "In addition to child support or maintenance already paid under a Child Support Agency assessment")
    IN_ADDITION_TO_CHILD_SUPPORT("In addition to child support"),
    @CCD(label = "To meet expenses arising from a child’s disability")
    DISABILITY_EXPENSES("disability expenses"),
    @CCD(label = "To meet expenses incurred by a child in relation to being educated or training for work")
    TRAINING("training"),
    @CCD(
            label = "When either the child or the person with care of the child or the absent parent of the child is not habitually resident in the United Kingdom"
    )
    WHEN_NOT_HABITUALLY_RESIDENT("When not habitually resident"),
    OTHER("Other");

    private final String value;

    @JsonValue
    public String getId() {
        return value;
    }

    public static ConsentNatureOfApplication forValue(String ccdType) {
        return Arrays.stream(ConsentNatureOfApplication.values())
            .filter(option -> option.value.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
