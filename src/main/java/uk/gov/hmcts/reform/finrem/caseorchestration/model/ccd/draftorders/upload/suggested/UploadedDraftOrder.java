package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadedDraftOrder {

    @JsonProperty("suggestedDraftOrderDocument")
    private CaseDocument suggestedDraftOrderDocument;

    @JsonProperty("additionalDocuments")
    private List<DocumentCollection> additionalDocuments;
}
