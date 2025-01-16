package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;

@RequiredArgsConstructor
@Getter
public enum OrderFiledBy {
    APPLICANT("The applicant"),
    RESPONDENT("The respondent"),
    APPLICANT_BARRISTER("The applicant barrister"),
    RESPONDENT_BARRISTER("The respondent barrister"),
    INTERVENER_1("The intervener 1"),
    INTERVENER_2("The intervener 2"),
    INTERVENER_3("The intervener 3"),
    INTERVENER_4("The intervener 4");

    @Getter(onMethod_ = @JsonValue)
    private final String value;

    @JsonCreator
    public static OrderFiledBy forValue(String value) {
        for (OrderFiledBy party : values()) {
            if (party.value.equals(value)) {
                return party;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    public static OrderFiledBy forUploadPartyValue(String uploadParty) {
        if (UPLOAD_PARTY_APPLICANT.equals(uploadParty)) {
            return APPLICANT;
        } else if (UPLOAD_PARTY_RESPONDENT.equals(uploadParty)) {
            return RESPONDENT;
        } else {
            throw new IllegalArgumentException("Unknown upload party: " + uploadParty);
        }
    }
}
