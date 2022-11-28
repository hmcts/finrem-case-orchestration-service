package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;


import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum EstimatedAsset {

    UNABLE_TO_QUANTIFY("estimatedAssetsChecklist_1"),
    UNDER_ONE_MILLION("estimatedAssetsChecklist_2"),
    ONE_TO_FIVE_MILLION("estimatedAssetsChecklist_3"),
    FIVE_TO_TEN_MILLION("estimatedAssetsChecklist_4"),
    OVER_TEN_MILLION("estimatedAssetsChecklist_5");
    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static EstimatedAsset getEstimatedAsset(String ccdType) {
        return Arrays.stream(EstimatedAsset.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
