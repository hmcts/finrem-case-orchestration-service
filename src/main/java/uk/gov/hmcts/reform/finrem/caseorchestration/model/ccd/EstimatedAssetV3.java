package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ms_estimatedAssetsChecklist3", generate = true)
@RequiredArgsConstructor
public enum EstimatedAssetV3 {

    @CCD(label = "Over £20 million")
    OVER_TWENTY_MILLION_POUNDS("overTwentyMillionPounds"),
    @CCD(label = "£10 million - £20 million")
    BETWEEN_TEN_TO_TWENTY_MILLION_POUNDS("betweenTenAndTwentyMillionPounds"),
    @CCD(label = "£5 million - £10 million")
    BETWEEN_FIVE_TO_TEN_MILLION_POUNDS("betweenFiveAndTenMillionPounds"),
    @CCD(label = "£1 million - £5 million")
    BETWEEN_ONE_TO_FIVE_MILLION_POUNDS("betweenOneAndFiveMillionPounds"),
    @CCD(label = "£500,000 - £1 million")
    BETWEEN_FIVE_HUNDRED_THOUSAND_TO_ONE_MILLION_POUNDS("betweenFiveHundredThousandAndOneMillionPounds"),
    @CCD(label = "£250,000 - £500,000")
    BETWEEN_TWO_HUNDRED_AND_FIFTY_THOUSAND_TO_FIVE_HUNDRED_THOUSAND_POUNDS("betweenTwoHundredAndFiftyThousandAndFiveHundredThousandPounds"),
    @CCD(label = "Under £250,000 (this should be total of combined net assets, but excluding pensions)")
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
