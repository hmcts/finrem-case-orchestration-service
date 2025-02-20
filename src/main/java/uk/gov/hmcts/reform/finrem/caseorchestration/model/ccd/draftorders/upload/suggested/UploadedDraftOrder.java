package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.AdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.UploadDraftOrderAdditionalDocument;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
    private List<AdditionalDocumentsCollection> additionalDocuments;

    @JsonIgnore
    @Override
    public List<CaseDocument> getUploadingDocuments() {
        return Stream.concat(
            Stream.ofNullable(suggestedDraftOrderDocument),
            Optional.ofNullable(additionalDocuments)
                .stream()
                .flatMap(Collection::stream)
                .map(AdditionalDocumentsCollection::getValue)
                .map(UploadDraftOrderAdditionalDocument::getOrderAttachment)
                .filter(Objects::nonNull)
        ).toList();
    }

}
