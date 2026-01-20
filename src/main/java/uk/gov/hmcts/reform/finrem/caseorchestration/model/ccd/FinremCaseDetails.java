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

import java.time.LocalDateTime;
import java.util.Optional;

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
    public Address getAppSolicitorAddress() {
        return isConsentedApplication()
            ? data.getContactDetailsWrapper().getSolicitorAddress()
            : data.getContactDetailsWrapper().getApplicantSolicitorAddress();
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

}
