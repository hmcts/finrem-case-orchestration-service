package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_UploadDraftOrderAdditionalDocument", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadDraftOrderAdditionalDocument {
    @CCD(label = "Document", regex = ".doc,.docx,.pdf", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument orderAttachment;
}
