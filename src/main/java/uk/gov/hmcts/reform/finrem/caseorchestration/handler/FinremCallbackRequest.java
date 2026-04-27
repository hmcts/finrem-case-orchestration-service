package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy.isSameOrganisation;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class FinremCallbackRequest {

    @JsonProperty("case_details")
    private FinremCaseDetails caseDetails;

    @JsonProperty("case_details_before")
    private FinremCaseDetails caseDetailsBefore;

    @JsonProperty("event_id")
    private EventType eventType;

    @JsonIgnore
    public FinremCaseData getFinremCaseData() {
        return ofNullable(getCaseDetails())
            .map(FinremCaseDetails::getData)
            .orElse(null);
    }

    @JsonIgnore
    public FinremCaseData getFinremCaseDataBefore() {
        return ofNullable(getCaseDetailsBefore())
            .map(FinremCaseDetails::getData)
            .orElse(null);
    }

    @JsonIgnore
    public boolean isApplicantSolicitorChanged() {
        boolean emailMismatch = !Objects.equals(
            normalizeAndLower(getFinremCaseDataBefore().getAppSolicitorEmailIfRepresented()),
            normalizeAndLower(getFinremCaseData().getAppSolicitorEmailIfRepresented())
        );

        return emailMismatch || !isSameOrganisation(
            getFinremCaseDataBefore().getApplicantOrganisationPolicy(),
            getFinremCaseData().getApplicantOrganisationPolicy());
    }

    @JsonIgnore
    public boolean isRespondentSolicitorChanged() {
        boolean emailMismatch = !Objects.equals(
            normalizeAndLower(getFinremCaseDataBefore().getRespSolicitorEmailIfRepresented()),
            normalizeAndLower(getFinremCaseData().getRespSolicitorEmailIfRepresented())
        );

        return emailMismatch || !isSameOrganisation(
            getFinremCaseDataBefore().getRespondentOrganisationPolicy(),
            getFinremCaseData().getRespondentOrganisationPolicy());
    }


    private String normalizeAndLower(String email) {
        return ofNullable(email)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .orElse(null);
    }
}