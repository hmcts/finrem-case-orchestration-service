package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FinremCaseDetails implements CcdCaseDetails<FinremCaseData> {

    private Long id;
    private String jurisdiction;
    private State state;
    private LocalDateTime createdDate;
    private Integer securityLevel;
    private String callbackResponseStatus;
    private LocalDateTime lastModified;
    private Classification securityClassification;
    @JsonProperty("case_data")
    @JsonAlias("data")
    private FinremCaseData data;

    @JsonProperty("case_type_id")
    private CaseType caseType;

    @JsonProperty("locked_by_user_id")
    private Integer lockedBy;

    @Setter(lombok.AccessLevel.NONE)
    private Integer version;

    @JsonIgnore
    public String getCaseIdAsString() {
        return Optional.ofNullable(id).map(String::valueOf).orElse(null);
    }

    @JsonIgnore
    public boolean isConsentedApplication() {
        return CaseType.CONSENTED.equals(caseType);
    }

    @JsonIgnore
    public boolean isContestedApplication() {
        return CaseType.CONTESTED.equals(caseType);
    }

    @JsonIgnore
    public String getAppSolicitorName() {
        return isConsentedApplication()
            ? data.getContactDetailsWrapper().getSolicitorName()
            : data.getContactDetailsWrapper().getApplicantSolicitorName();
    }

    @JsonIgnore
    public String getRespSolicitorName() {
        return data.getContactDetailsWrapper().getRespondentSolicitorName();
    }

    @JsonIgnore
    public String getAppSolicitorEmail() {
        return isConsentedApplication()
            ? data.getContactDetailsWrapper().getSolicitorEmail()
            : data.getContactDetailsWrapper().getApplicantSolicitorEmail();
    }

    @JsonIgnore
    public String getApplicantSolicitorRef() {
        return data.getContactDetailsWrapper().getSolicitorReference();
    }

    @JsonIgnore
    public String getRespSolicitorEmail() {
        return data.getContactDetailsWrapper().getRespondentSolicitorEmail();
    }

    @JsonIgnore
    public String getRespSolicitorRef() {
        return data.getContactDetailsWrapper().getRespondentSolicitorReference();
    }

    @JsonIgnore
    public String getAppSolicitorFirm() {
        return isConsentedApplication()
            ? data.getContactDetailsWrapper().getSolicitorFirm()
            : data.getContactDetailsWrapper().getApplicantSolicitorFirm();
    }

    @JsonIgnore
    public boolean isApplicantSolicitorAgreeToReceiveEmails() {
        return isContestedApplication()
            ? YesOrNo.YES.equals(data.getContactDetailsWrapper().getApplicantSolicitorConsentForEmails())
            : YesOrNo.YES.equals(data.getContactDetailsWrapper().getSolicitorAgreeToReceiveEmails());
    }

    @JsonIgnore
    public boolean isConsentedInContestedCase() {
        return isContestedApplication() && data.getConsentOrderWrapper().getConsentD81Question() != null;
    }

    @JsonIgnore
    public String getRespondentFullName() {
        return isContestedApplication()
            ? data.getFullRespondentNameContested()
            : data.getFullRespondentNameConsented();
    }

    /**
     * Determines whether the applicant's solicitor is digitally registered and can receive
     * correspondence electronically.
     *
     * <p>A solicitor is considered "digital" when all of the following hold:
     * <ul>
     *     <li>the applicant is marked as represented ({@code applicantRepresented} is {@link YesOrNo#YES});</li>
     *     <li>an {@link OrganisationPolicy} is set on the case with a non-blank
     *         {@link Organisation#getOrganisationID() organisation ID}; and</li>
     *     <li>a non-blank applicant solicitor email address is present, as returned by
     *         {@link #getAppSolicitorEmail()}.</li>
     * </ul>
     *
     * @return {@code true} if the applicant is represented by a solicitor with a linked
     *         organisation and a solicitor email address; {@code false} otherwise
     */
    @JsonIgnore
    public boolean isApplicantSolicitorDigital() {
        FinremCaseData finremCaseData = getData();
        ContactDetailsWrapper contactDetailsWrapper = finremCaseData.getContactDetailsWrapper();

        if (!YesOrNo.isYes(contactDetailsWrapper.getApplicantRepresented())) {
            return false;
        }

        String orgId = ofNullable(finremCaseData.getApplicantOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse("");

        return isNotBlank(orgId) && isNotBlank(getAppSolicitorEmail());
    }

    /**
     * Determines whether the respondent's solicitor is digitally registered and can receive
     * correspondence electronically.
     *
     * <p>A solicitor is considered "digital" when all of the following hold:
     * <ul>
     *     <li>the respondent is marked as represented in either a consented case
     *         ({@code consentedRespondentRepresented} is {@link YesOrNo#YES}) or a contested case
     *         ({@code contestedRespondentRepresented} is {@link YesOrNo#YES});</li>
     *     <li>an {@link OrganisationPolicy} is set on the case with a non-blank
     *         {@link Organisation#getOrganisationID() organisation ID}; and</li>
     *     <li>a non-blank respondent solicitor email address is present, as returned by
     *         {@link #getRespSolicitorEmail()}.</li>
     * </ul>
     *
     * @return {@code true} if the respondent is represented by a solicitor with a linked
     *         organisation and a solicitor email address; {@code false} otherwise
     */
    @JsonIgnore
    public boolean isRespondentSolicitorDigital() {
        FinremCaseData finremCaseData = getData();
        ContactDetailsWrapper contactDetailsWrapper = finremCaseData.getContactDetailsWrapper();

        if (!YesOrNo.isYes(contactDetailsWrapper.getConsentedRespondentRepresented())
            && !YesOrNo.isYes(contactDetailsWrapper.getContestedRespondentRepresented())) {
            return false;
        }

        String orgId = ofNullable(finremCaseData.getRespondentOrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse("");

        return isNotBlank(orgId) && isNotBlank(getRespSolicitorEmail());
    }
}
