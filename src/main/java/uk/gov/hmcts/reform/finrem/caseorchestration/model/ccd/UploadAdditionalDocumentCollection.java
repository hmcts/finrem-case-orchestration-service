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
public class UploadAdditionalDocumentCollection implements CaseDocumentsDiscovery {
    private UploadAdditionalDocument value;

    @Override
    public List<CaseDocument> discover() {
        return ofNullable(value)
            .map(UploadAdditionalDocument::getAdditionalDocuments)
            .map(List::of)
            .orElse(List.of());
    }
}
