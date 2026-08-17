package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_UploadDraftOrderAdditionalDocument", generate = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalDocumentsCollection implements HasCaseDocument {
    private UploadDraftOrderAdditionalDocument value;
}
