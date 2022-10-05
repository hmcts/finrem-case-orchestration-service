package uk.gov.hmcts.reform.finrem.caseorchestration.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FlagDetails {

    @JsonProperty("name")
    private String name;
    @JsonProperty("hearingRelevant")
    private boolean hearingRelevant;
    @JsonProperty("flagComment")
    private boolean flagComment;
    @JsonProperty("flagCode")
    private String flagCode;
    @JsonProperty("isParent")
    private boolean isParent;
    @JsonProperty("Path")
    private List<String> path;
    @JsonProperty("childFlags")
    private List<FlagDetails> childFlags;
    @JsonProperty("listOfValuesLength")
    private Integer listOfValuesLength;
    @JsonProperty("listOfValues")
    private List<Object> listOfValues;

}
