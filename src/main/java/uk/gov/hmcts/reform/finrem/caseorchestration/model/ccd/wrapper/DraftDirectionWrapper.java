package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentsDiscovery;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftDirectionWrapper implements CaseDocumentsDiscovery {
    private List<DraftDirectionOrderCollection> draftDirectionOrderCollection;
    private DraftDirectionOrder latestDraftDirectionOrder;
    private List<DraftDirectionOrderCollection> judgesAmendedOrderCollection;
    private List<DraftDirectionDetailsCollection> draftDirectionDetailsCollection;
    private List<DraftDirectionDetailsCollection> draftDirectionDetailsCollectionRO;

    @Override
    public List<CaseDocument> discover() {
        return Stream.of(
                Stream.of(ofNullable(latestDraftDirectionOrder).orElse(DraftDirectionOrder.builder().build()).getUploadDraftDocument()),
                ofNullable(draftDirectionOrderCollection)
                    .orElse(List.of())
                    .stream()
                    .flatMap(d -> d.discover().stream()),
                ofNullable(judgesAmendedOrderCollection)
                    .orElse(List.of())
                    .stream()
                    .flatMap(d -> d.discover().stream())
            )
            .flatMap(s -> s)
            .filter(Objects::nonNull)
            .toList();
    }
}
