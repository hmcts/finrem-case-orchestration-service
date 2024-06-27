package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentsDiscovery;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralApplicationsCollection implements CaseDocumentsDiscovery {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("value")
    private GeneralApplicationItems value;

    @Override
    public List<CaseDocument> discover() {
        return Stream.of(
                ofNullable(value)
                    .map(GeneralApplicationItems::getGeneralApplicationDocument)
                    .map(List::of)
                    .orElse(List.of()),
                ofNullable(value)
                    .map(GeneralApplicationItems::getGeneralApplicationDraftOrder)
                    .map(List::of)
                    .orElse(List.of()),
                ofNullable(value)
                    .map(GeneralApplicationItems::getGeneralApplicationDirectionsDocument)
                    .map(List::of)
                    .orElse(List.of())
            )
            .flatMap(List::stream)
            .toList();
    }
}
