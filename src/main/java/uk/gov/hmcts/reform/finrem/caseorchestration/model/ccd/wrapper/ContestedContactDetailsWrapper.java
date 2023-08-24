package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class ContestedContactDetailsWrapper extends ContactDetailsWrapper {

    private String applicantSolicitorName;
    private String applicantSolicitorFirm;
    private Address applicantSolicitorAddress;
    private String applicantSolicitorEmail;
    private String applicantSolicitorPhone;
    @JsonProperty("applicantSolicitorDXnumber")
    private String applicantSolicitorDxNumber;
    private YesOrNo applicantSolicitorConsentForEmails;
    @JsonProperty("respondentFMName")
    private String respondentFmName;
    @JsonProperty("respondentLName")
    private String respondentLname;
    @JsonProperty("respondentRepresented")
    private YesOrNo contestedRespondentRepresented;
}
