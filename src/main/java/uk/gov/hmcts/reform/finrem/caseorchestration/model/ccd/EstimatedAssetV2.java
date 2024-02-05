package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;


import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * New version for the assets estimation list with updated values,
 * we must retain the old fixed list for old cases hence this new v2 one.
 */
@RequiredArgsConstructor
public enum EstimatedAssetV2 {

    OVER_FIFTEEN_MILLION_POUNDS("overFifteenMillionPounds"),
    BETWEEN_SEVEN_POINT_FIVE_TO_FIFTEEN_MILLION_POUNDS("betweenSevenPointFiveAndFifteenMillionPounds"),
    BETWEEN_ONE_TO_SEVEN_POINT_FIVE_MILLION_POUNDS("betweenOneAndSevenPointFiveMillionPounds"),
    UNDER_ONE_MILLION_POUNDS("underOneMillionPounds"),
    UNABLE_TO_QUANTIFY("unableToQuantify");
    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static EstimatedAssetV2 getEstimatedAsset(String ccdType) {
        return Arrays.stream(EstimatedAssetV2.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
