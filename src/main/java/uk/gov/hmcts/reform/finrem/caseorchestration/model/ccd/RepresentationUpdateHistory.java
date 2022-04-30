package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RepresentationUpdateHistory {
    @JsonProperty("RepresentationUpdateHistory")
    private List<Element<RepresentationUpdate>> representationUpdateHistory;

}
