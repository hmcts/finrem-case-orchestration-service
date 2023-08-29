package uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralOrderDetails implements DocumentTemplateDetails {
    @JsonProperty("DivorceCaseNumber")
    private String divorceCaseNumber;
    @JsonProperty("ApplicantName")
    private String applicantName;
    @JsonProperty("RespondentName")
    private String respondentName;
    @JsonProperty("GeneralOrderCourt")
    private String generalOrderCourt;
    @JsonProperty("GeneralOrderHeaderOne")
    private String generalOrderHeaderOne;
    @JsonProperty("GeneralOrderHeaderTwo")
    private String generalOrderHeaderTwo;
    @JsonProperty("GeneralOrderCourtSitting")
    private String generalOrderCourtSitting;
    @JsonProperty("GeneralOrderJudgeDetails")
    private String generalOrderJudgeDetails;
    @JsonProperty("GeneralOrderRecitals")
    private String generalOrderRecitals;
    @JsonProperty("GeneralOrderDate")
    private String generalOrderDate;
    @JsonProperty("GeneralOrderBodyText")
    private String generalOrderBodyText;
}
