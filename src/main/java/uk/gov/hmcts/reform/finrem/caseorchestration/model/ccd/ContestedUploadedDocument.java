package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContestedUploadedDocument {
    @JsonProperty("caseDocuments")
    private CaseDocument caseDocuments;

    @JsonProperty("caseDocumentType")
    private String caseDocumentType;

    @JsonProperty("caseDocumentParty")
    private String caseDocumentParty;

    @JsonProperty("caseDocumentFdr")
    private String caseDocumentFdr;

    @JsonProperty("caseDocumentConfidential")
    private String caseDocumentConfidential;

    @JsonProperty("caseDocumentOther")
    private String caseDocumentOther;

    @JsonProperty("hearingDetails")
    private String hearingDetails;

    @JsonProperty("DocumentEmailContent")
    private String documentEmailContent;

    @JsonProperty("DocumentDateAdded")
    private Date documentDateAdded;

    @JsonProperty("DocumentComment")
    private String documentComment;

    @JsonProperty("DocumentFileName")
    private String documentFileName;
}
