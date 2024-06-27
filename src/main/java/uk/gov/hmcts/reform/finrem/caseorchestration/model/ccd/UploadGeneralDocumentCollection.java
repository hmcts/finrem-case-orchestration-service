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
public class UploadGeneralDocumentCollection implements CaseDocumentCollection<UploadGeneralDocument>, CaseDocumentsDiscovery {
    private UploadGeneralDocument value;

    @Override
    public List<CaseDocument> discover() {
        return ofNullable(value)
            .map(UploadGeneralDocument::getDocumentLink)
            .map(List::of)
            .orElse(List.of());
    }
}
