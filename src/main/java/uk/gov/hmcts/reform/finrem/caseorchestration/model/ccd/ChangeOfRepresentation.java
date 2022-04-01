package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeOfRepresentation {
    @JsonProperty("party") // app/resp
    String party;

    @JsonProperty("name") // client name
    String clientName;

    @JsonProperty("date") //today date
    LocalDate date;

    @JsonProperty("by") //caseworker's email
    String by;

    @JsonProperty("via") // notice of change hardcoded
    String via;

    @JsonProperty("added") // added rep
    ChangedRepresentative added;

    @JsonProperty("removed") // removed rep
    ChangedRepresentative removed;
}
