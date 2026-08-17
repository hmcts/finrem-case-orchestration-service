package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangedRepresentative {
    @CCD(label = "Name", searchable = false)
    @JsonProperty("name")
    private String name;

    @CCD(label = "Email", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("email")
    private String email;

    @CCD(label = "Organisation", searchable = false)
    @JsonProperty("organisation")
    private Organisation organisation;
}
