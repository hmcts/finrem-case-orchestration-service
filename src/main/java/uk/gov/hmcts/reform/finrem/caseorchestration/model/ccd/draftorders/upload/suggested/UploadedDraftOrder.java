package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadedDraftOrder implements HasUploadingDocuments {

    @JsonProperty("suggestedDraftOrderDocument")
    private CaseDocument suggestedDraftOrderDocument;

    @JsonProperty("additionalDocuments")
    private List<DocumentCollection> suggestedDraftOrderAdditionalDocumentsCollection;

    @Override
    @JsonIgnore
    public List<CaseDocument> getUploadingDocuments() {
        List<CaseDocument> ret = new ArrayList<>();
        Optional.ofNullable(suggestedDraftOrderDocument).ifPresent(ret::add);
        Optional.ofNullable(suggestedDraftOrderAdditionalDocumentsCollection)
            .ifPresent(collection -> collection.stream()
                .map(DocumentCollection::getValue)
                .filter(Objects::nonNull)
                .forEach(ret::add));
        return ret;
    }

}
