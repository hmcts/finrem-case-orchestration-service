package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_DocumentType", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtherDocument implements HasCaseDocument {
    @CCD(
            label = "Type of document",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FR_fl_OtherDocument"
    )
    private OtherDocumentType typeOfDocument;
    @CCD(
            label = "Document",
            categoryID = "applicationsConsentOrderToFinaliseProceedings",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private CaseDocument uploadedDocument;
}
