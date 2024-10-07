package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class DraftOrder implements HasCaseDocument {

    @JsonProperty("suggestedDraftOrderDocument")
    CaseDocument suggestedDraftOrderDocument;

    @JsonProperty("additionalDocuments")
    private List<AdditionalDocumentsCollection> additionalDocuments;

}
