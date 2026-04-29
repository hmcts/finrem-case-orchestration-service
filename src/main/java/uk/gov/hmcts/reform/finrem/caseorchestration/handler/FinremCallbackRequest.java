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

import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy.isSameOrganisation;
import static uk.gov.hmcts.reform.finrem.caseorchestration.utils.EmailUtils.areEmailsDifferent;

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

    /**
     * Checks if the applicant's solicitor details have changed between the current
     * and previous case state.
     *
     * <p>
     * A change is detected if either the normalized email address or the
     * organization policy (ID) differs. Emails are compared after trimming
     * whitespace and converting to lowercase.
     * </p>
     *
     * @return {@code true} if the applicant solicitor email or policy has changed;
     *         {@code false} otherwise.
     */
    @JsonIgnore
    public boolean hasApplicantSolicitorChanged() {
        return isSolicitorChanged(
            FinremCaseData::getAppSolicitorEmailIfRepresented,
            FinremCaseData::getApplicantOrganisationPolicy
        );
    }

    /**
     * Checks if the respondent's solicitor details have changed between the current
     * and previous case state.
     *
     * <p>
     * Follows the same comparison logic as {@link #hasApplicantSolicitorChanged()},
     * focusing on respondent-specific fields.
     * </p>
     *
     * @return {@code true} if the respondent solicitor email or policy has changed;
     *         {@code false} otherwise.
     */
    @JsonIgnore
    public boolean hasRespondentSolicitorChanged() {
        return isSolicitorChanged(
            FinremCaseData::getRespSolicitorEmailIfRepresented,
            FinremCaseData::getRespondentOrganisationPolicy
        );
    }

    private boolean isSolicitorChanged(Function<FinremCaseData, String> emailExtractor,
                                       Function<FinremCaseData, OrganisationPolicy> policyExtractor) {

        String currentEmail = ofNullable(getFinremCaseData()).map(emailExtractor).orElse(null);
        String beforeEmail = ofNullable(getFinremCaseDataBefore()).map(emailExtractor).orElse(null);

        if (areEmailsDifferent(currentEmail, beforeEmail)) {
            return true;
        }

        OrganisationPolicy currentPolicy = ofNullable(getFinremCaseData()).map(policyExtractor).orElse(null);
        OrganisationPolicy beforePolicy = ofNullable(getFinremCaseDataBefore()).map(policyExtractor).orElse(null);

        if (currentPolicy == null  && beforePolicy == null) {
            return false;
        }

        return !isSameOrganisation(currentPolicy, beforePolicy);
    }
}
