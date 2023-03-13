package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContestedChildrenDetailDataElement {
    @JsonProperty("childrenLivesInEnglandOrWales")
    public String childrenLivesInEnglandOrWales;
    @JsonProperty("childFullname")
    public String childFullname;
    @JsonProperty("childDateOfBirth")
    public String childDateOfBirth;
    @JsonProperty("childGender")
    public String childGender;
    @JsonProperty("childApplicantRelation")
    public String childApplicantRelation;
    @JsonProperty("childApplicantRelationOther")
    public String childApplicantRelationOther;
    @JsonProperty("childRespondentRelation")
    public String childRespondentRelation;
    @JsonProperty("childRespondentRelationOther")
    public String childRespondentRelationOther;
}
