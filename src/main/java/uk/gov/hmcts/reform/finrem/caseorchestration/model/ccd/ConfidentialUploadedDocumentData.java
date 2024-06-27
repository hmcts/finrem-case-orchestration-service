package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseDocumentTabData;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Optional.ofNullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class ConfidentialUploadedDocumentData implements CaseDocumentTabData, CaseDocumentsDiscovery {
    private String id;
    private UploadConfidentialDocument value;

    @Override
    @JsonIgnore
    public String getElementId() {
        return this.id;
    }

    @Override
    @JsonIgnore
    public void setUploadDateTime(LocalDateTime date) {
        if (value != null) {
            value.setConfidentialDocumentUploadDateTime(date);
        }
    }

    @Override
    public List<CaseDocument> discover() {
        return ofNullable(value)
            .map(UploadConfidentialDocument::getDocumentLink)
            .map(List::of)
            .orElse(List.of());
    }
}
