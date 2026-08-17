package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed;

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

@ComplexType(name = "FR_AgreedPensionSharingAnnexes", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgreedPensionSharingAnnex implements HasUploadingDocuments {

    @CCD(
            label = "Document",
            hint = "You must upload Microsoft Word or PDF documents. Document names should clearly reflect the party name, the type of hearing and the date of the hearing. For example \"JonesFDA11Jul24\"",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("agreedPensionSharingAnnexes")
    private CaseDocument agreedPensionSharingAnnexes;

    @JsonIgnore
    @Override
    public List<CaseDocument> getUploadingDocuments() {
        return agreedPensionSharingAnnexes == null ? List.of() : List.of(agreedPensionSharingAnnexes);
    }
}
