package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed;

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

    @JsonProperty("agreedDraftOrderDocument")
    private CaseDocument agreedDraftOrderDocument;

    @JsonProperty("resubmission")
    private List<String> resubmission;

    @JsonProperty("additionalDocuments")
    private List<DocumentCollection> agreedDraftOrderAdditionalDocumentsCollection;

    @Override
    public List<CaseDocument> getUploadingDocuments() {
        List<CaseDocument> ret = new ArrayList<>();
        Optional.ofNullable(agreedDraftOrderDocument).ifPresent(ret::add);
        Optional.ofNullable(agreedDraftOrderAdditionalDocumentsCollection)
            .ifPresent(collection -> collection.stream()
                .map(DocumentCollection::getValue)
                .filter(Objects::nonNull)
                .forEach(ret::add));
        return ret;
    }

}
