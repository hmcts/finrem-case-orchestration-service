package uk.gov.hmcts.reform.finrem.caseorchestration.ccd.datamigration.model.prod;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDMigrationRequest {
    private String token;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("case_details")
    private CaseDetails caseDetails;
}
