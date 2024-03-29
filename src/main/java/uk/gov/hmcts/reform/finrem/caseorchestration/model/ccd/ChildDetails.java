package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ChildRelation;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChildDetails {

    private String childFullname;
    private LocalDate childDateOfBirth;
    private Gender childGender;
    private ChildRelation childApplicantRelation;
    private String childApplicantRelationOther;
    private ChildRelation childRespondentRelation;
    private String childRespondentRelationOther;
    @JsonProperty("childrenLivesInEnglandOrWales")
    private YesOrNo childrenLiveInEnglandOrWales;
}
