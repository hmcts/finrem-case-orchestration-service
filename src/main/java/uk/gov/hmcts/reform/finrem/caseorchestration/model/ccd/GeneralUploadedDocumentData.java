package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseDocumentTabData;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralUploadedDocumentData implements CaseDocumentTabData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private GeneralUploadedDocument generalUploadedDocument;


    @Override
    @JsonIgnore
    public String getElementId() {
        return id;
    }

    @Override
    @JsonIgnore
    public void setUploadDateTime(LocalDateTime dateTime) {
        if (generalUploadedDocument != null) {
            generalUploadedDocument.setGeneralDocumentUploadDateTime(dateTime);
        }
    }
}

