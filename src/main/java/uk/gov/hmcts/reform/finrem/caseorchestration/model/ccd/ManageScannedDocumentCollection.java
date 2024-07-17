package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ManageScannedDocumentCollection implements HasCaseDocument {

    private String id;

    @EqualsAndHashCode.Exclude
    @JsonProperty("value")
    private ManageScannedDocument manageScannedDocument;

    public UploadCaseDocumentCollection toUploadCaseDocumentCollection() {
        return UploadCaseDocumentCollection.builder()
            .id(id)
            .uploadCaseDocument(manageScannedDocument.getUploadCaseDocument())
            .build();
    }
}
