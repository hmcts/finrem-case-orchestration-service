package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FR_GeneralLetterDocument", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralLetter implements HasCaseDocument {
    @CCD(label = "Document", searchable = false, typeOverride = FieldType.Document)
    @JsonProperty("generatedLetter")
    private CaseDocument generatedLetter;
    @CCD(
            label = "Upload Document",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("generalLetterUploadedDocument")
    private CaseDocument generalLetterUploadedDocument;
    @CCD(
            label = "Uploaded Document(s)",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document"
    )
    @JsonProperty("generalLetterUploadedDocuments")
    private List<DocumentCollectionItem> generalLetterUploadedDocuments;
}
