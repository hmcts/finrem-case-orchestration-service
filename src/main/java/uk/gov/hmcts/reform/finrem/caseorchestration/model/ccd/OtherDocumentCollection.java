package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class OtherDocumentCollection implements CaseDocumentsDiscovery {
    private OtherDocument value;

    @Override
    public List<CaseDocument> discover() {
        return ofNullable(value)
            .map(OtherDocument::getUploadedDocument)
            .map(List::of)
            .orElse(List.of());
    }
}
