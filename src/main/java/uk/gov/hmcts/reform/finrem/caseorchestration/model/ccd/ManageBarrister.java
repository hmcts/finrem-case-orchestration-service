package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManageBarrister {
    @JsonProperty("name")
    private String name;

    @JsonProperty("eMail")
    private String email;

    @JsonProperty("phoneNumber")
    private String phone;

    @JsonProperty("organisation")
    private Organisation organisation;
}
