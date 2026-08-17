package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentationUpdate {
    @CCD(label = "Party", searchable = false)
    @JsonProperty("party")
    String party;

    @CCD(label = "Client Name", searchable = false)
    @JsonProperty("name")
    String clientName;

    @CCD(label = "Date", searchable = false)
    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    LocalDateTime date;

    @CCD(label = "Updated by", searchable = false)
    @JsonProperty("by")
    String by;

    @CCD(label = "Updated via", searchable = false)
    @JsonProperty("via")
    String via;

    @CCD(label = "Added representative", searchable = false)
    @JsonProperty("added")
    ChangedRepresentative added;

    @CCD(label = "Removed representative", searchable = false)
    @JsonProperty("removed")
    ChangedRepresentative removed;
}
