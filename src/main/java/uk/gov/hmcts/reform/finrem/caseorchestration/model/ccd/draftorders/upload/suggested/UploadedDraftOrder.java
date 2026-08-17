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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasUploadingDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.AdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.UploadDraftOrderAdditionalDocument;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_SuggestedDraftOrders", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadedDraftOrder implements HasUploadingDocuments {

    @CCD(
            label = "Document",
            hint = "You must upload Microsoft Word documents. Document names should clearly reflect the party name, the type of hearing and the date of the hearing. For example \"JonesFDA11Jul24\"",
            regex = ".doc,.docx",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("suggestedDraftOrderDocument")
    private CaseDocument suggestedDraftOrderDocument;

    @CCD(
            label = "Upload additional attachments",
            hint = "You must upload Microsoft Word or PDF documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_UploadDraftOrderAdditionalDocument"
    )
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
