package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum AuthorisationSignedBy {
    APPLICANT("Applicant"),
    LITIGATION_FRIEND("Litigation Friend"),
    APPLICANT_SOLICITOR("Applicant's solicitor");

    private final String id;

    @JsonValue
    public String getId() {
        return id;
    }

    public static AuthorisationSignedBy getAuthorisationSignedBy(String ccdType) {
        return Arrays.stream(AuthorisationSignedBy.values())
            .filter(option -> option.id.equals(ccdType))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
