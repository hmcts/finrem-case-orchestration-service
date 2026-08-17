package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ms_estimatedAssetsChecklist", generate = true)
@RequiredArgsConstructor
public enum EstimatedAsset {

    @CCD(label = "Unable to quantify")
    UNABLE_TO_QUANTIFY("estimatedAssetsChecklist_1"),
    @CCD(label = "Under £1 million")
    UNDER_ONE_MILLION("estimatedAssetsChecklist_2"),
    @CCD(label = "£1 - £5 million")
    ONE_TO_FIVE_MILLION("estimatedAssetsChecklist_3"),
    @CCD(label = "£5 - £10 million")
    FIVE_TO_TEN_MILLION("estimatedAssetsChecklist_4"),
    @CCD(label = "Over £10 million")
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
