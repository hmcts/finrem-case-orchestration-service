package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredArgsConstructor
public enum BenefitPaymentChecklist {

    STEP_CHILD_OR_STEP_CHILDREN("Step child or step children"),
    IN_ADDITION_TO_CHILD_SUPPORT_MAINTENANCE_ALREADY_PAID(
        "In addition to child support maintenance already paid under a Child Support Agency assessment"),
    EXPENSES_ARISING_FROM_A_CHILDS_DISABILITY("To meet expenses arising from a child’s disability"),
    EXPENSES_INCURRED_BY_A_CHILD_BEING_IN_EDUCATED_OR_TRAINING_FOR_WORK(
        "To meet expenses incurred by a child being in educated or training for work"),
    NOT_HABITUALLY_RESIDENT_IN_THE_UNITED_KINGDOM(
        "The child or the person with care of the child or the absent parent of the child is not habitually resident in the United Kingdom");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static BenefitPaymentChecklist forValue(String value) {
        return Arrays.stream(BenefitPaymentChecklist.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
