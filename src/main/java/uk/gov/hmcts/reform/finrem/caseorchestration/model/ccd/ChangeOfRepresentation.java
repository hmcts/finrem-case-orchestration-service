package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
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
}
