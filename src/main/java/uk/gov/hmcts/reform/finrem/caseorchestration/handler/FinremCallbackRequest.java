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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

import java.util.Objects;
import java.util.function.Function;

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
        return isSolicitorChanged(
            FinremCaseData::getAppSolicitorEmailIfRepresented,
            FinremCaseData::getApplicantOrganisationPolicy
        );
    }

    @JsonIgnore
    public boolean isRespondentSolicitorChanged() {
        return isSolicitorChanged(
            FinremCaseData::getRespSolicitorEmailIfRepresented,
            FinremCaseData::getRespondentOrganisationPolicy
        );
    }

    private boolean isSolicitorChanged(Function<FinremCaseData, String> emailExtractor,
                                       Function<FinremCaseData, OrganisationPolicy> policyExtractor) {

        String currentEmail = ofNullable(getFinremCaseData()).map(emailExtractor).orElse(null);
        String beforeEmail = ofNullable(getFinremCaseDataBefore()).map(emailExtractor).orElse(null);

        boolean emailMismatch = !Objects.equals(normalizeAndLower(currentEmail), normalizeAndLower(beforeEmail));

        if (emailMismatch) {
            return true;
        }

        OrganisationPolicy currentPolicy = ofNullable(getFinremCaseData()).map(policyExtractor).orElse(null);
        OrganisationPolicy beforePolicy = ofNullable(getFinremCaseDataBefore()).map(policyExtractor).orElse(null);

        return !isSameOrganisation(currentPolicy, beforePolicy);
    }

    private String normalizeAndLower(String email) {
        return ofNullable(email)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .orElse(null);
    }
}
