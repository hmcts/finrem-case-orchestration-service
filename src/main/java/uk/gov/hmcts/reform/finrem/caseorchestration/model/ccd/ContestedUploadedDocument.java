package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestedUploadedDocument {
    @JsonProperty("caseDocuments")
    private CaseDocument caseDocuments;

    @JsonProperty("caseDocumentType")
    private String caseDocumentType;

    @JsonProperty("caseDocumentParty")
    private String caseDocumentParty;

    @JsonProperty("caseDocumentConfidential")
    private String caseDocumentConfidential;

    @JsonProperty("caseDocumentOther")
    private String caseDocumentOther;
}
