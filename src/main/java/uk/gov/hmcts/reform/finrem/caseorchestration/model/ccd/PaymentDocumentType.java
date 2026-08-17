package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_fl_paymentDocumentType", generate = true)
@RequiredArgsConstructor
public enum PaymentDocumentType {
    COPY_OF_PAPER_FORM_A("Copy of paper form A");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static PaymentDocumentType forValue(String value) {
        return Arrays.stream(PaymentDocumentType.values())
            .filter(option -> option.getValue().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
