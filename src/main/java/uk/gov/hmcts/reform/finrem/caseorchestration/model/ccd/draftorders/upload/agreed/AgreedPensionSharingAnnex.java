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

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgreedPensionSharingAnnex implements HasUploadingDocuments {

    @JsonProperty("agreedPensionSharingAnnexes")
    private CaseDocument agreedPensionSharingAnnexes;

    @JsonIgnore
    @Override
    public List<CaseDocument> getUploadingDocuments() {
        return agreedPensionSharingAnnexes == null ? List.of() : List.of(agreedPensionSharingAnnexes);
    }
}
