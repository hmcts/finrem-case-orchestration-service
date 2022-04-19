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
    @JsonProperty("party")
    String party;

    @JsonProperty("name")
    String clientName;

    @JsonProperty("date")
    LocalDate date;

    @JsonProperty("by")
    String by;

    @JsonProperty("via")
    String via;

    @JsonProperty("added")
    ChangedRepresentative added;

    @JsonProperty("removed")
    ChangedRepresentative removed;
}
