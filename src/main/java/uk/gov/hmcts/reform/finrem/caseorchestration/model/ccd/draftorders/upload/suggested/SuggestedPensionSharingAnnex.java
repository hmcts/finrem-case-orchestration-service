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

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_SuggestedPensionSharingAnnexes", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestedPensionSharingAnnex implements HasUploadingDocuments {

    @CCD(
            label = "Document",
            hint = "Document names should clearly reflect the party name, document type and the date",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("suggestedPensionSharingAnnexes")
    private CaseDocument suggestedPensionSharingAnnexes;

    @JsonIgnore
    @Override
    public List<CaseDocument> getUploadingDocuments() {
        return suggestedPensionSharingAnnexes == null ? List.of() : List.of(suggestedPensionSharingAnnexes);
    }
}
