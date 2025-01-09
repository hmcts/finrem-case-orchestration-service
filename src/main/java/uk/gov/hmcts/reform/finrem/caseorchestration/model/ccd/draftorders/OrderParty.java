package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;

@RequiredArgsConstructor
@Getter
public enum OrderParty {
    APPLICANT("Applicant"),
    RESPONDENT("Respondent");

    @Getter(onMethod_ = @JsonValue)
    private final String value;

    @JsonCreator
    public static OrderParty forValue(String value) {
        for (OrderParty party : values()) {
            if (party.value.equals(value)) {
                return party;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    public static OrderParty forUploadPartyValue(String uploadParty) {
        if (UPLOAD_PARTY_APPLICANT.equals(uploadParty)) {
            return APPLICANT;
        } else if (UPLOAD_PARTY_RESPONDENT.equals(uploadParty)) {
            return RESPONDENT;
        } else {
            throw new IllegalArgumentException("Unknown upload party: " + uploadParty);
        }
    }
}
