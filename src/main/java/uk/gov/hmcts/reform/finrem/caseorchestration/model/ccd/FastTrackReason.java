package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ms_fast_track_reason", generate = true)
@RequiredArgsConstructor
public enum FastTrackReason {
    @CCD(
            label = "The financial remedy sought is an application to vary a periodical payments order (but does not seek to dismiss the periodical payments order and substitute it with one or more of the following: a lump sum order, a property adjustment order, a pension sharing order or a pension compensation sharing order)"
    )
    PERIODICAL_PAYMENTS_ORDER_NOT_SEEK_TO_DISMISS("reason_1"),
    @CCD(
            label = "The financial remedy sought is related to the recognition and enforcement of a foreign maintenance order under Article 56 of the Maintenance Regulation or Article 10 of the 2007 Hague Convention"
    )
    RECOGNITION_AND_ENFORCEMENT("reason_2"),
    @CCD(label = "The financial remedy sought is only for an order for periodical payments")
    ORDER_FOR_PERIODICAL_PAYMENTS("reason_3"),
    @CCD(
            label = "The financial remedy sought is related to an order for financial provision during a marriage or civil partnership under the Domestic Proceedings and Magistrates’ Courts Act 1978 or Schedule 6 to the Civil Partnership Act 2004"
    )
    FINANCIAL_PROVISION("reason_4");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static FastTrackReason getFastTrackReason(String ccdType) {
        return Arrays.stream(FastTrackReason.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
