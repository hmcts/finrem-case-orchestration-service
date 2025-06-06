package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DraftDirectionOrder implements HasCaseDocument {

    @JsonProperty("purposeOfDocument")
    String purposeOfDocument;

    @JsonProperty("uploadDraftDocument")
    CaseDocument uploadDraftDocument;

    @JsonProperty("additionalDocuments")
    private List<DocumentCollectionItem> additionalDocuments;
}
