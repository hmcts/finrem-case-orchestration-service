package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseDocument {

    @JsonProperty("document_url")
    private String documentUrl;
    @JsonProperty("document_filename")
    private String documentFilename;
    @JsonProperty("document_binary_url")
    private String documentBinaryUrl;
    @JsonProperty("category_id")
    private String categoryId;

    public CaseDocument(CaseDocument caseDocuments) {
        this.documentUrl = caseDocuments.getDocumentUrl();
        this.documentFilename = caseDocuments.getDocumentFilename();
        this.documentBinaryUrl = caseDocuments.getDocumentBinaryUrl();
        this.categoryId = caseDocuments.getCategoryId();
    }
}
