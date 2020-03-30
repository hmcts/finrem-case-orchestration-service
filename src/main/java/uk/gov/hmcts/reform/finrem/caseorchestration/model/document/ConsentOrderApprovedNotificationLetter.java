package uk.gov.hmcts.reform.finrem.caseorchestration.model.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
public class ConsentOrderApprovedNotificationLetter {

    @JsonProperty("addressee")
    private String addressee;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("applicantName")
    private String applicantName;

    @JsonProperty("respondentName")
    private String respondentName;

    @JsonProperty("letterDate")
    private String letterDate;

    @JsonProperty("formattedAddress")
    private String formattedAddress;

    @JsonProperty("caseNumber")
    private String caseNumber;
}
