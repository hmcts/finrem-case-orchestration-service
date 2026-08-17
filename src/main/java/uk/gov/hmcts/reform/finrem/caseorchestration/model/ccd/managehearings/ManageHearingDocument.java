package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;

import java.util.UUID;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_manageHearingDocument", generate = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManageHearingDocument {
    @CCD(label = " ", searchable = false, typeOverride = FieldType.Text)
    private UUID hearingId;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.Text)
    private CaseDocumentType hearingCaseDocumentType;
    @CCD(label = " ", categoryID = "hearingNotices", searchable = false, typeOverride = FieldType.Document)
    private CaseDocument hearingDocument;
}
