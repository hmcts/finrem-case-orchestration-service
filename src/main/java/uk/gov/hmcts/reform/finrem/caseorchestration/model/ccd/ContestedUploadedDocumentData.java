package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseDocumentData;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContestedUploadedDocumentData implements CaseDocumentData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private ContestedUploadedDocument uploadedCaseDocument;

    @Override
    @JsonIgnore
    public String getElementId() {
        return this.id;
    }

    @Override
    @JsonIgnore
    public void setUploadDateTime(LocalDateTime date) {
        if (uploadedCaseDocument != null) {
            uploadedCaseDocument.setCaseDocumentUploadDateTime(date);
        }
    }
}