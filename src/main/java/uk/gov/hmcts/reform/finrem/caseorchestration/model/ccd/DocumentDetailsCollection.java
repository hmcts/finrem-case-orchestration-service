package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailsCollection {

    @JsonProperty("caseDocument")
    private String caseDocument;

    @JsonProperty("caseDocumentType")
    private String caseDocumentType;

    @JsonProperty("caseDocumentOther")
    private String caseDocumentOther;

    @JsonProperty("caseDocumentParty")
    private String caseDocumentParty;

    @JsonProperty("hearingDetails")
    private String hearingDetails;
}