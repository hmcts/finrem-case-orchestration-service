package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseDocumentTabData;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UploadCaseDocumentCollection implements CaseDocumentTabData {

    private String id;

    @EqualsAndHashCode.Exclude
    @JsonProperty("value")
    private UploadCaseDocument uploadCaseDocument;

    private YesOrNo selectForUpdate;

    @Override
    @JsonIgnore
    public String getElementId() {
        return this.id;
    }

    @Override
    @JsonIgnore
    public void setUploadDateTime(LocalDateTime date) {
        if (uploadCaseDocument != null && uploadCaseDocument.getCaseDocumentUploadDateTime() == null) {
            uploadCaseDocument.setCaseDocumentUploadDateTime(date);
        }
    }
}
