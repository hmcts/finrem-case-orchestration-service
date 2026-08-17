package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ms_estimatedAssetsChecklist2", generate = true)
@RequiredArgsConstructor
public enum EstimatedAssetV2 {

    @CCD(label = "Over £15 million")
    OVER_FIFTEEN_MILLION_POUNDS("overFifteenMillionPounds"),
    @CCD(label = "£7.5 - £15 million")
    BETWEEN_SEVEN_POINT_FIVE_TO_FIFTEEN_MILLION_POUNDS("betweenSevenPointFiveAndFifteenMillionPounds"),
    @CCD(label = "£1 - £7.5 million")
    BETWEEN_ONE_TO_SEVEN_POINT_FIVE_MILLION_POUNDS("betweenOneAndSevenPointFiveMillionPounds"),
    @CCD(label = "Under £1 million")
    UNDER_ONE_MILLION_POUNDS("underOneMillionPounds"),
    @CCD(label = "Under £250,000 (this should be total of combined net assets, but excluding pensions)")
    UNDER_TWO_HUNDRED_AND_FIFTY_THOUSAND_POUNDS("underTwoHundredAndFiftyThousandPounds"),
    @CCD(label = "Unable to quantify")
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
