package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralLetter implements HasCaseDocument {
    @JsonProperty("generatedLetter")
    private CaseDocument generatedLetter;
    @JsonProperty("generalLetterUploadedDocument")
    private CaseDocument generalLetterUploadedDocument;
    @JsonProperty("generalLetterUploadedDocuments")
    private List<DocumentCollectionItem> generalLetterUploadedDocuments;
}
