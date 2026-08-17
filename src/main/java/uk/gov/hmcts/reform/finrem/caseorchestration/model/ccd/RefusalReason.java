package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_ms_refusalReason", generate = true)
@RequiredArgsConstructor
public enum RefusalReason {
    @CCD(label = "If other (please specify in text box provided)")
    FR_MS_REFUSAL_REASON_1("FR_ms_refusalReason_1"),
    @CCD(
            label = "Please provide a breakdown of the pension values/property values as it is not possible to understand the values of what each party will receive."
    )
    FR_MS_REFUSAL_REASON_2("FR_ms_refusalReason_2"),
    @CCD(
            label = "The proposed order does not appear to be fair taking account of S25 Matrimonial Causes Act 1973. The parties are requested to explain more fully the thinking behind the order and why it is fair."
    )
    FR_MS_REFUSAL_REASON_3("FR_ms_refusalReason_3"),
    @CCD(label = "Entire case to be transferred to the Applicant’s home court to consider listing directions")
    FR_MS_REFUSAL_REASON_4("FR_ms_refusalReason_4"),
    @CCD(
            label = "Financial Remedy application to be transferred to the Applicant’s home court to consider listing directions"
    )
    FR_MS_REFUSAL_REASON_5("FR_ms_refusalReason_5"),
    @CCD(
            label = "Application should be fixed for hearing on first available date for 20 minutes when the Court will consider whether the draft order should be approved. Both parties should attend and if they do not do so the Court may not approve the order"
    )
    FR_MS_REFUSAL_REASON_6("FR_ms_refusalReason_6"),
    @CCD(label = "The D81 form is incomplete")
    FR_MS_REFUSAL_REASON_7("FR_ms_refusalReason_7"),
    @CCD(label = "It is unclear whether the Respondent has obtained independent legal advice")
    FR_MS_REFUSAL_REASON_8("FR_ms_refusalReason_8"),
    @CCD(label = "The pension annex has not been attached")
    FR_MS_REFUSAL_REASON_9("FR_ms_refusalReason_9"),
    @CCD(
            label = "Insufficient information has been provided as to the children’s housing needs and whether they are met by the order"
    )
    FR_MS_REFUSAL_REASON_10("FR_ms_refusalReason_10"),
    @CCD(
            label = "Insufficient information has been provided as to the parties’ pension provision if the order were effected"
    )
    FR_MS_REFUSAL_REASON_11("FR_ms_refusalReason_11"),
    @CCD(
            label = "Insufficient information has been provided as to the justification for departure from equality of capital"
    )
    FR_MS_REFUSAL_REASON_12("FR_ms_refusalReason_12"),
    @CCD(
            label = "Insufficient information has been provided as to the parties’ housing needs and whether they are met by the order"
    )
    FR_MS_REFUSAL_REASON_13("FR_ms_refusalReason_13"),
    @CCD(
            label = "Insufficient information has been provided as to the parties’ capital positions if the order were effected"
    )
    FR_MS_REFUSAL_REASON_14("FR_ms_refusalReason_14");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RefusalReason forValue(String value) {
        return Arrays.stream(RefusalReason.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
