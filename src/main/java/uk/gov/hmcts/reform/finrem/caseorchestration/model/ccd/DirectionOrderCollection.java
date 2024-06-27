package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DirectionOrderCollection implements CaseDocumentsDiscovery {

    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private DirectionOrder value;

    @Override
    public List<CaseDocument> discover() {
        return ofNullable(value)
            .map(DirectionOrder::getUploadDraftDocument)
            .map(List::of)
            .orElse(List.of());
    }
}
