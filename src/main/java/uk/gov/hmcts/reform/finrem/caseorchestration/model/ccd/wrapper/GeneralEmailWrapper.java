package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralEmailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralEmailWrapper implements HasCaseDocument {
    @CCD(
            label = "Recipient's email",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @TemporaryField
    private String generalEmailRecipient;
    @CCD(
            label = "Email created by",
            searchable = false,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @TemporaryField
    private String generalEmailCreatedBy;
    @CCD(
            label = "Please fill in the body of the email",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudAccess.class}
    )
    @TemporaryField
    private String generalEmailBody;
    @CCD(
            label = "Upload Document",
            hint = "File size must not exceed 2MB.",
            categoryID = "administrativeDocumentsTransitional",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @TemporaryField
    private CaseDocument generalEmailUploadedDocument;
    @CCD(
            label = "Upload Document(s)",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Document",
            access = {CaseworkerDivorceFinancialremedyCourtadminCrudPlus1RolesEnaquoAccess.class}
    )
    @TemporaryField
    private List<DocumentCollectionItem> generalEmailUploadedDocuments;

    // It stores the emails sent
    @CCD(
            label = "General Email",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FR_GeneralEmailDocument",
            access = {CaseworkerDivorceFinancialremedyCourtadminCudPlus1RolesZbqgyjAccess.class}
    )
    private List<GeneralEmailCollection> generalEmailCollection;

    /**
     * Returns the uploaded general email documents.
     *
     * @return the uploaded documents, or an empty list if none exist
     */
    @JsonIgnore
    public List<CaseDocument> getUploadedDocuments() {
        return emptyIfNull(generalEmailUploadedDocuments)
            .stream()
            .filter(Objects::nonNull)
            .map(DocumentCollectionItem::getValue)
            .filter(Objects::nonNull)
            .toList();
    }
}
