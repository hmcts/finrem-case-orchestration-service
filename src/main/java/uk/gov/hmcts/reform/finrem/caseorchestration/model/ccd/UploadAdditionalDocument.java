package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_uploadAdditionalDocument", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadAdditionalDocument implements HasCaseDocument {
    @CCD(
            label = "Please upload any additional documents related to your application",
            categoryID = "applicationsMainApplication",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private CaseDocument additionalDocuments;
    @CCD(
            label = "Document type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_s_documentType"
    )
    private AdditionalDocumentType additionalDocumentType;
}
