package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum EstimatedAssetV3 {

    OVER_TWENTY_MILLION_POUNDS("overTwentyMillionPounds"),
    BETWEEN_TEN_TO_TWENTY_MILLION_POUNDS("betweenTenAndTwentyMillionPounds"),
    BETWEEN_FIVE_TO_TEN_MILLION_POUNDS("betweenFiveAndTenMillionPounds"),
    BETWEEN_ONE_TO_FIVE_MILLION_POUNDS("betweenOneAndFiveMillionPounds"),
    BETWEEN_FIVE_HUNDRED_THOUSAND_TO_ONE_MILLION_POUNDS("betweenFiveHundredThousandAndOneMillionPounds"),
    BETWEEN_TWO_HUNDRED_AND_FIFTY_THOUSAND_TO_FIVE_HUNDRED_THOUSAND_POUNDS("betweenTwoHundredAndFiftyThousandAndFiveHundredThousandPounds"),
    UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS("underTwoHundredAndFiftyThousandPounds");
    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static EstimatedAssetV3 getEstimatedAsset(String ccdType) {
        return Arrays.stream(EstimatedAssetV3.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
